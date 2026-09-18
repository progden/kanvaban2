package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Board Aggregate Root：對應 spec-kanban-basic.md 的 {@code board}，內部管理 {@link Swimlane}、
 * {@link Stage} 兩種子物件（不是獨立 Aggregate，只能透過 {@code Board} 的方法異動）。
 *
 * <p>新建立的 Board 依 spec-kanban-basic.md 各 Feature Background 逐字描述的狀態，預設帶 1 個
 * Swimlane「預設泳道」與 3 個 Stage「待辦」「進行中」「完成」，滿足 board→swimlane／board→stage
 * 「至少保留一個」的關係限制（見 implementation-loop OQ-IMPL-12）。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）依既有慣例由呼叫端查詢
 * {@code BoardMembership}（F02，T-04 尚未實作）後才呼叫，{@code Board} 本身不驗證（見 OQ-IMPL-13）。
 */
public final class Board {

    private static final String DEFAULT_SWIMLANE_NAME = "預設泳道";
    private static final List<String> DEFAULT_STAGE_NAMES = List.of("待辦", "進行中", "完成");

    private final UUID id;
    private String name;
    private final UUID createdBy;
    private final List<Swimlane> swimlanes;
    private final List<Stage> stages;
    private final List<ActivityRecord> activityLog;
    private final CardLookupPort cardLookupPort;
    private List<CardSummary> cardSummaries;

    private Board(UUID id, String name, UUID createdBy, List<Swimlane> swimlanes, List<Stage> stages,
            List<ActivityRecord> activityLog, CardLookupPort cardLookupPort) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.swimlanes = swimlanes;
        this.stages = stages;
        this.activityLog = activityLog;
        this.cardLookupPort = cardLookupPort;
        this.cardSummaries = List.of();
    }

    public static Board create(UUID operatorId, String name, CardLookupPort cardLookupPort) {
        if (name == null || name.isBlank()) {
            throw new DomainException(ErrorCode.BOARD_NAME_BLANK, "看板名稱不可為空");
        }
        List<Swimlane> initialSwimlanes = new ArrayList<>();
        initialSwimlanes.add(Swimlane.create(DEFAULT_SWIMLANE_NAME, 1));
        List<Stage> initialStages = new ArrayList<>();
        int order = 1;
        for (String stageName : DEFAULT_STAGE_NAMES) {
            initialStages.add(Stage.create(stageName, order));
            order++;
        }
        Board board = new Board(UUID.randomUUID(), name, operatorId, initialSwimlanes, initialStages,
                new ArrayList<>(), cardLookupPort);
        board.recordActivity(operatorId, "建立看板");
        return board;
    }

    public static Board reconstruct(UUID id, String name, UUID createdBy, List<SwimlaneSnapshot> swimlaneSnapshots,
            List<StageSnapshot> stageSnapshots, List<ActivityRecordSnapshot> activitySnapshots,
            CardLookupPort cardLookupPort) {
        List<Swimlane> swimlanes = new ArrayList<>();
        for (SwimlaneSnapshot snapshot : swimlaneSnapshots) {
            swimlanes.add(Swimlane.reconstruct(snapshot.id(), snapshot.name(), snapshot.order()));
        }
        swimlanes.sort(Comparator.comparingInt(Swimlane::getOrder));

        List<Stage> stages = new ArrayList<>();
        for (StageSnapshot snapshot : stageSnapshots) {
            stages.add(Stage.reconstruct(snapshot.id(), snapshot.name(), snapshot.order(), snapshot.role()));
        }
        stages.sort(Comparator.comparingInt(Stage::getOrder));

        List<ActivityRecord> activityLog = new ArrayList<>();
        for (ActivityRecordSnapshot snapshot : activitySnapshots) {
            activityLog.add(ActivityRecord.reconstruct(
                    snapshot.id(), snapshot.operatorId(), snapshot.action(), snapshot.occurredAt()));
        }

        return new Board(id, name, createdBy, swimlanes, stages, activityLog, cardLookupPort);
    }

    // ---- 讀取 ----

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public List<Swimlane> getSwimlanes() {
        return List.copyOf(swimlanes);
    }

    public List<Stage> getStages() {
        return List.copyOf(stages);
    }

    public List<ActivityRecord> getActivityLog() {
        return List.copyOf(activityLog);
    }

    public void refreshCardSummaries() {
        this.cardSummaries = cardLookupPort == null ? List.of() : List.copyOf(cardLookupPort.findByBoardId(id));
    }

    public int countCardsInSwimlane(UUID swimlaneId) {
        return (int) cardSummaries.stream().filter(c -> c.swimlaneId().equals(swimlaneId)).count();
    }

    public int countCardsInStage(UUID stageId) {
        return (int) cardSummaries.stream().filter(c -> c.stageId().equals(stageId)).count();
    }

    // ---- Swimlane ----

    public Swimlane addSwimlane(UUID operatorId, String name) {
        Swimlane swimlane = Swimlane.create(name, swimlanes.size() + 1);
        swimlanes.add(swimlane);
        recordActivity(operatorId, "新增 Swimlane「" + name + "」");
        return swimlane;
    }

    public void renameSwimlane(UUID operatorId, UUID swimlaneId, String newName) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        swimlane.rename(newName);
        recordActivity(operatorId, "重新命名 Swimlane 為「" + newName + "」");
    }

    public void moveSwimlaneBefore(UUID operatorId, UUID swimlaneId, UUID beforeSwimlaneId) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        swimlanes.remove(swimlane);
        int insertIndex = swimlanes.size();
        if (beforeSwimlaneId != null) {
            insertIndex = indexOfSwimlane(beforeSwimlaneId);
        }
        swimlanes.add(insertIndex, swimlane);
        renumberSwimlanes();
        recordActivity(operatorId, "調整 Swimlane 順序");
    }

    public Swimlane ensureSwimlaneRemovable(UUID swimlaneId) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        if (swimlanes.size() <= 1) {
            throw new DomainException(ErrorCode.MINIMUM_SWIMLANE, "看板至少需要保留一個 Swimlane");
        }
        return swimlane;
    }

    public void removeSwimlane(UUID operatorId, UUID swimlaneId) {
        Swimlane swimlane = ensureSwimlaneRemovable(swimlaneId);
        int cardCount = countCardsInSwimlane(swimlaneId);
        if (cardCount > 0) {
            throw new DomainException(ErrorCode.SWIMLANE_HAS_CARDS,
                    "該 Swimlane 內有 " + cardCount + " 張卡片，需先刪除或一併確認刪除");
        }
        swimlanes.remove(swimlane);
        renumberSwimlanes();
        recordActivity(operatorId, "刪除 Swimlane「" + swimlane.getName() + "」");
    }

    // ---- Stage ----

    public Stage addStage(UUID operatorId, String name, UUID beforeStageId) {
        int insertIndex = stages.size();
        if (beforeStageId != null) {
            insertIndex = indexOfStage(beforeStageId);
        }
        Stage stage = Stage.create(name, insertIndex + 1);
        stages.add(insertIndex, stage);
        renumberStages();
        recordActivity(operatorId, "新增 Stage「" + name + "」");
        return stage;
    }

    public void renameStage(UUID operatorId, UUID stageId, String newName) {
        Stage stage = findStage(stageId);
        stage.rename(newName);
        recordActivity(operatorId, "重新命名 Stage 為「" + newName + "」");
    }

    public void moveStageBefore(UUID operatorId, UUID stageId, UUID beforeStageId) {
        Stage stage = findStage(stageId);
        stages.remove(stage);
        int insertIndex = stages.size();
        if (beforeStageId != null) {
            insertIndex = indexOfStage(beforeStageId);
        }
        stages.add(insertIndex, stage);
        renumberStages();
        recordActivity(operatorId, "調整 Stage 順序");
    }

    public Stage ensureStageRemovable(UUID stageId) {
        Stage stage = findStage(stageId);
        if (stages.size() <= 1) {
            throw new DomainException(ErrorCode.MINIMUM_STAGE, "看板至少需要保留一個 Stage");
        }
        return stage;
    }

    public void removeStage(UUID operatorId, UUID stageId) {
        Stage stage = ensureStageRemovable(stageId);
        int cardCount = countCardsInStage(stageId);
        if (cardCount > 0) {
            throw new DomainException(ErrorCode.STAGE_HAS_CARDS,
                    "該 Stage 內有 " + cardCount + " 張卡片，需先轉移到其他 Stage");
        }
        stages.remove(stage);
        renumberStages();
        recordActivity(operatorId, "刪除 Stage「" + stage.getName() + "」");
    }

    public void setStageRole(UUID operatorId, UUID stageId, StageRole role) {
        Stage target = findStage(stageId);
        if (role == StageRole.START || role == StageRole.DONE) {
            for (Stage stage : stages) {
                if (stage.getRole() == role && !stage.getId().equals(stageId)) {
                    stage.changeRole(StageRole.NONE);
                }
            }
        }
        target.changeRole(role);
        recordActivity(operatorId, "設定 Stage「" + target.getName() + "」角色為 " + role);
    }

    // ---- 私有輔助 ----

    private void recordActivity(UUID operatorId, String action) {
        activityLog.add(new ActivityRecord(operatorId, action, Instant.now()));
    }

    private Swimlane findSwimlane(UUID swimlaneId) {
        return findSwimlaneOptional(swimlaneId)
                .orElseThrow(() -> new DomainException(ErrorCode.SWIMLANE_NOT_FOUND, "找不到指定的 Swimlane"));
    }

    private Optional<Swimlane> findSwimlaneOptional(UUID swimlaneId) {
        return swimlanes.stream().filter(s -> s.getId().equals(swimlaneId)).findFirst();
    }

    private int indexOfSwimlane(UUID swimlaneId) {
        for (int i = 0; i < swimlanes.size(); i++) {
            if (swimlanes.get(i).getId().equals(swimlaneId)) {
                return i;
            }
        }
        throw new DomainException(ErrorCode.SWIMLANE_NOT_FOUND, "找不到指定的 Swimlane");
    }

    private void renumberSwimlanes() {
        for (int i = 0; i < swimlanes.size(); i++) {
            swimlanes.get(i).reorder(i + 1);
        }
    }

    private Stage findStage(UUID stageId) {
        return findStageOptional(stageId)
                .orElseThrow(() -> new DomainException(ErrorCode.STAGE_NOT_FOUND, "找不到指定的 Stage"));
    }

    private Optional<Stage> findStageOptional(UUID stageId) {
        return stages.stream().filter(s -> s.getId().equals(stageId)).findFirst();
    }

    private int indexOfStage(UUID stageId) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getId().equals(stageId)) {
                return i;
            }
        }
        throw new DomainException(ErrorCode.STAGE_NOT_FOUND, "找不到指定的 Stage");
    }

    private void renumberStages() {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).reorder(i + 1);
        }
    }
}

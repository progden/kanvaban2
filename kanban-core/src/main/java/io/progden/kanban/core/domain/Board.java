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
 * 「至少保留一個」的關係限制（見 implementation-loop OQ-IMPL-14）。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）依既有慣例由呼叫端查詢
 * {@code BoardMembership}（F02，T-04 尚未實作）後才呼叫，{@code Board} 本身不驗證（見 OQ-IMPL-15）。
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
    private final BoardClock clock;
    private List<CardSummary> cardSummaries;

    private Board(UUID id, String name, UUID createdBy, List<Swimlane> swimlanes, List<Stage> stages,
            List<ActivityRecord> activityLog, CardLookupPort cardLookupPort, BoardClock clock) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.swimlanes = swimlanes;
        this.stages = stages;
        this.activityLog = activityLog;
        this.cardLookupPort = cardLookupPort;
        this.clock = clock;
        this.cardSummaries = List.of();
    }

    public static Board create(UUID operatorId, String name, CardLookupPort cardLookupPort, Instant systemNow) {
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
                new ArrayList<>(), cardLookupPort, BoardClock.initial());
        board.recordActivity(operatorId, "建立看板", systemNow);
        return board;
    }

    public static Board reconstruct(UUID id, String name, UUID createdBy, List<SwimlaneSnapshot> swimlaneSnapshots,
            List<StageSnapshot> stageSnapshots, List<ActivityRecordSnapshot> activitySnapshots,
            CardLookupPort cardLookupPort, BoardClockSnapshot clockSnapshot) {
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

        return new Board(id, name, createdBy, swimlanes, stages, activityLog, cardLookupPort,
                BoardClock.reconstruct(clockSnapshot));
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

    public ClockStatus getClockStatus() {
        return clock.getStatus();
    }

    public Instant getClockTime(Instant systemNow) {
        return clock.now(systemNow);
    }

    public boolean isClockPaused() {
        return clock.isPaused();
    }

    public BoardClockSnapshot getClockSnapshot() {
        return clock.snapshot();
    }

    /**
     * 供其他 Aggregate（例如 {@code Card}）的寫入操作取得這個 board 的「新事件時間」：套用
     * {@code uc-guard-clock-monotonicity} 的單調性檢查，通過才更新看板的 {@code lastEventAt} 基準並
     * 回傳可用的時間戳記；呼叫端（application 層）拿到之後要連同 board 一併存回去，否則基準不會生效。
     */
    public Instant newEventTime(Instant systemNow) {
        return clock.recordEventTime(systemNow);
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

    /**
     * 供 {@code Card} 的寫入操作（{@code uc-add-card}／{@code uc-move-card-swimlane}）確認目的
     * {@code swimlane} 真的存在、且屬於這個 {@code board}（CR-011）；不存在時拋出
     * {@link ErrorCode#SWIMLANE_NOT_FOUND}，呼叫端不需另外承接回傳值。
     */
    public void ensureSwimlaneExists(UUID swimlaneId) {
        findSwimlane(swimlaneId);
    }

    /**
     * 供 {@code Card} 的寫入操作（{@code uc-add-card}／{@code uc-move-card-stage}）確認目的
     * {@code stage} 真的存在、且屬於這個 {@code board}（CR-011）；不存在時拋出
     * {@link ErrorCode#STAGE_NOT_FOUND}，呼叫端不需另外承接回傳值。
     */
    public void ensureStageExists(UUID stageId) {
        findStage(stageId);
    }

    // ---- Swimlane ----

    public Swimlane addSwimlane(UUID operatorId, String name, Instant systemNow) {
        Swimlane swimlane = Swimlane.create(name, swimlanes.size() + 1);
        swimlanes.add(swimlane);
        recordActivity(operatorId, "新增 Swimlane「" + name + "」", systemNow);
        return swimlane;
    }

    public void renameSwimlane(UUID operatorId, UUID swimlaneId, String newName, Instant systemNow) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        swimlane.rename(newName);
        recordActivity(operatorId, "重新命名 Swimlane 為「" + newName + "」", systemNow);
    }

    public void moveSwimlaneBefore(UUID operatorId, UUID swimlaneId, UUID beforeSwimlaneId, Instant systemNow) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        swimlanes.remove(swimlane);
        int insertIndex = swimlanes.size();
        if (beforeSwimlaneId != null) {
            insertIndex = indexOfSwimlane(beforeSwimlaneId);
        }
        swimlanes.add(insertIndex, swimlane);
        renumberSwimlanes();
        recordActivity(operatorId, "調整 Swimlane 順序", systemNow);
    }

    public Swimlane ensureSwimlaneRemovable(UUID swimlaneId) {
        Swimlane swimlane = findSwimlane(swimlaneId);
        if (swimlanes.size() <= 1) {
            throw new DomainException(ErrorCode.MINIMUM_SWIMLANE, "看板至少需要保留一個 Swimlane");
        }
        return swimlane;
    }

    public void removeSwimlane(UUID operatorId, UUID swimlaneId, Instant systemNow) {
        Swimlane swimlane = ensureSwimlaneRemovable(swimlaneId);
        int cardCount = countCardsInSwimlane(swimlaneId);
        if (cardCount > 0) {
            throw new DomainException(ErrorCode.SWIMLANE_HAS_CARDS,
                    "該 Swimlane 內有 " + cardCount + " 張卡片，需先刪除或一併確認刪除");
        }
        swimlanes.remove(swimlane);
        renumberSwimlanes();
        recordActivity(operatorId, "刪除 Swimlane「" + swimlane.getName() + "」", systemNow);
    }

    // ---- Stage ----

    public Stage addStage(UUID operatorId, String name, UUID beforeStageId, Instant systemNow) {
        int insertIndex = stages.size();
        if (beforeStageId != null) {
            insertIndex = indexOfStage(beforeStageId);
        }
        Stage stage = Stage.create(name, insertIndex + 1);
        stages.add(insertIndex, stage);
        renumberStages();
        recordActivity(operatorId, "新增 Stage「" + name + "」", systemNow);
        return stage;
    }

    public void renameStage(UUID operatorId, UUID stageId, String newName, Instant systemNow) {
        Stage stage = findStage(stageId);
        stage.rename(newName);
        recordActivity(operatorId, "重新命名 Stage 為「" + newName + "」", systemNow);
    }

    public void moveStageBefore(UUID operatorId, UUID stageId, UUID beforeStageId, Instant systemNow) {
        Stage stage = findStage(stageId);
        stages.remove(stage);
        int insertIndex = stages.size();
        if (beforeStageId != null) {
            insertIndex = indexOfStage(beforeStageId);
        }
        stages.add(insertIndex, stage);
        renumberStages();
        recordActivity(operatorId, "調整 Stage 順序", systemNow);
    }

    public Stage ensureStageRemovable(UUID stageId) {
        Stage stage = findStage(stageId);
        if (stages.size() <= 1) {
            throw new DomainException(ErrorCode.MINIMUM_STAGE, "看板至少需要保留一個 Stage");
        }
        return stage;
    }

    public void removeStage(UUID operatorId, UUID stageId, Instant systemNow) {
        Stage stage = ensureStageRemovable(stageId);
        int cardCount = countCardsInStage(stageId);
        if (cardCount > 0) {
            throw new DomainException(ErrorCode.STAGE_HAS_CARDS,
                    "該 Stage 內有 " + cardCount + " 張卡片，需先轉移到其他 Stage");
        }
        stages.remove(stage);
        renumberStages();
        recordActivity(operatorId, "刪除 Stage「" + stage.getName() + "」", systemNow);
    }

    /**
     * 驗證 {@code destinationStageId} 是這個 Board 底下真實存在、且不等於來源 {@code sourceStageId}
     * 的 Stage（{@code uc-delete-stage} 協調卡片轉移用，見 BoardApplicationService）。
     */
    public void ensureValidDestinationStage(UUID sourceStageId, UUID destinationStageId) {
        if (destinationStageId.equals(sourceStageId)) {
            throw new DomainException(ErrorCode.INVALID_DESTINATION_STAGE, "目的 Stage 不可與來源 Stage 相同");
        }
        findStage(destinationStageId);
    }

    public void setStageRole(UUID operatorId, UUID stageId, StageRole role, Instant systemNow) {
        Stage target = findStage(stageId);
        if (role == StageRole.START || role == StageRole.DONE) {
            for (Stage stage : stages) {
                if (stage.getRole() == role && !stage.getId().equals(stageId)) {
                    stage.changeRole(StageRole.NONE);
                }
            }
        }
        target.changeRole(role);
        recordActivity(operatorId, "設定 Stage「" + target.getName() + "」角色為 " + role, systemNow);
    }

    // ---- Board Clock（CR-004，spec-board-clock.md） ----

    /**
     * {@code uc-adjust-board-clock}：{@code r-board-owner} 權限檢查由呼叫端負責（依既有慣例，見
     * {@code BoardApplicationService} 類別註解）。調整動作本身不受單調性限制（可以調回過去），
     * 但這個動作被記錄的活動紀錄要用調整後的看板時間，且不更新 {@code lastEventAt} 基準——否則後續
     * {@code uc-guard-clock-monotonicity} 的判斷基準會被這次調整污染。
     */
    public void adjustClock(UUID operatorId, Instant newTime, Instant systemNow) {
        clock.adjustTo(newTime, systemNow);
        recordClockActivity(operatorId, "調整看板時間為 " + newTime, systemNow);
    }

    /** {@code uc-pause-resume-board-clock}：暫停看板時鐘。 */
    public void pauseClock(UUID operatorId, Instant systemNow) {
        clock.pause(systemNow);
        recordClockActivity(operatorId, "暫停看板時間", systemNow);
    }

    /**
     * {@code uc-pause-resume-board-clock}：恢復看板時鐘。時鐘不是 PAUSED 狀態時不動作、不記錄活動
     * 紀錄（design-board-clock.md 第 6 節：spec 只定義「時鐘目前為暫停狀態」時的恢復）。
     */
    public void resumeClock(UUID operatorId, Instant systemNow) {
        if (!clock.isPaused()) {
            return;
        }
        clock.resume(systemNow);
        recordClockActivity(operatorId, "恢復看板時間", systemNow);
    }

    // ---- 私有輔助 ----

    private void recordActivity(UUID operatorId, String action, Instant systemNow) {
        Instant eventTime = clock.recordEventTime(systemNow);
        activityLog.add(new ActivityRecord(operatorId, action, eventTime));
    }

    /**
     * 看板時鐘控制動作（調整／暫停／恢復）自己的活動紀錄：時間戳記用調整後的看板時間（唯讀，不guard），
     * 不透過 {@link #recordActivity} 是因為那條路徑會更新 {@code lastEventAt}，讓調回過去這個動作
     * 本身被誤判為「早於最後一筆事件」（見類別註解與 {@link #adjustClock}）。
     */
    private void recordClockActivity(UUID operatorId, String action, Instant systemNow) {
        activityLog.add(new ActivityRecord(operatorId, action, clock.now(systemNow)));
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

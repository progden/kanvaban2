package io.progden.kanban.spring.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.progden.kanban.core.domain.ActivityRecord;
import io.progden.kanban.core.domain.BoardClockSnapshot;
import io.progden.kanban.core.domain.ClockStatus;
import io.progden.kanban.core.domain.Stage;
import io.progden.kanban.core.domain.Swimlane;

/**
 * {@code Board} 的 JPA 對應表格，只在 persistence 層使用，不外流到 domain／application 層。
 *
 * <p>Swimlane／Stage／ActivityRecord 皆以 {@code board} 的子集合儲存（cascade + orphanRemoval），
 * 儲存時採「就地調和」（見 {@link #replaceSwimlanes}／{@link #replaceStages}／{@link #replaceActivityLog}）
 * 而非整批清除重建，避免同一個 UUID 主鍵在同一次 flush 內先刪後插發生衝突。
 */
@Entity
@Table(name = "boards")
public class BoardJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<SwimlaneJpaEntity> swimlanes = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<StageJpaEntity> stages = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ActivityRecordJpaEntity> activityLog = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "clock_status", nullable = false)
    private ClockStatus clockStatus;

    @Column(name = "clock_offset_millis", nullable = false)
    private long clockOffsetMillis;

    @Column(name = "clock_paused_at")
    private Instant clockPausedAt;

    @Column(name = "clock_last_event_at", nullable = false)
    private Instant clockLastEventAt;

    @Column(name = "clock_last_system_now")
    private Instant clockLastSystemNow;

    protected BoardJpaEntity() {
    }

    public BoardJpaEntity(UUID id, String name, UUID createdBy) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    /** 就地寫入 {@code Board} 目前的 {@link BoardClockSnapshot}，比照 Swimlane／Stage 的調和模式。 */
    public void updateClock(BoardClockSnapshot snapshot) {
        this.clockStatus = snapshot.status();
        this.clockOffsetMillis = snapshot.offsetMillis();
        this.clockPausedAt = snapshot.pausedAt();
        this.clockLastEventAt = snapshot.lastEventAt();
        this.clockLastSystemNow = snapshot.lastSystemNow();
    }

    public BoardClockSnapshot toClockSnapshot() {
        return new BoardClockSnapshot(clockStatus, clockOffsetMillis, clockPausedAt, clockLastEventAt,
                clockLastSystemNow);
    }

    public void replaceSwimlanes(List<Swimlane> source) {
        swimlanes.removeIf(existing -> source.stream().noneMatch(s -> s.getId().equals(existing.getId())));
        for (Swimlane swimlane : source) {
            SwimlaneJpaEntity existing = findSwimlane(swimlane.getId());
            if (existing == null) {
                swimlanes.add(new SwimlaneJpaEntity(swimlane.getId(), this, swimlane.getName(), swimlane.getOrder()));
            } else {
                existing.update(swimlane.getName(), swimlane.getOrder());
            }
        }
        swimlanes.sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()));
    }

    public void replaceStages(List<Stage> source) {
        stages.removeIf(existing -> source.stream().noneMatch(s -> s.getId().equals(existing.getId())));
        for (Stage stage : source) {
            StageJpaEntity existing = findStage(stage.getId());
            if (existing == null) {
                stages.add(new StageJpaEntity(stage.getId(), this, stage.getName(), stage.getOrder(), stage.getRole()));
            } else {
                existing.update(stage.getName(), stage.getOrder(), stage.getRole());
            }
        }
        stages.sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()));
    }

    public void replaceActivityLog(List<ActivityRecord> source) {
        for (ActivityRecord record : source) {
            if (findActivity(record.getId()) == null) {
                activityLog.add(new ActivityRecordJpaEntity(
                        record.getId(), this, record.getOperatorId(), record.getAction(), record.getOccurredAt()));
            }
        }
    }

    private SwimlaneJpaEntity findSwimlane(UUID id) {
        return swimlanes.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private StageJpaEntity findStage(UUID id) {
        return stages.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private ActivityRecordJpaEntity findActivity(UUID id) {
        return activityLog.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public List<SwimlaneJpaEntity> getSwimlanes() {
        return swimlanes;
    }

    public List<StageJpaEntity> getStages() {
        return stages;
    }

    public List<ActivityRecordJpaEntity> getActivityLog() {
        return activityLog;
    }
}

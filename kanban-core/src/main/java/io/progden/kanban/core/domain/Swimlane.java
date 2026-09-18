package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * Swimlane（泳道）：{@code Board} 的內部實體，只能透過 {@link Board} 的方法異動（不是獨立 Aggregate）。
 */
public final class Swimlane {

    private final UUID id;
    private String name;
    private int order;

    private Swimlane(UUID id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    static Swimlane create(String name, int order) {
        validateName(name);
        return new Swimlane(UUID.randomUUID(), name, order);
    }

    static Swimlane reconstruct(UUID id, String name, int order) {
        return new Swimlane(id, name, order);
    }

    void rename(String newName) {
        validateName(newName);
        this.name = newName;
    }

    void reorder(int newOrder) {
        this.order = newOrder;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException(ErrorCode.EMPTY_SWIMLANE_NAME, "Swimlane 名稱不可為空");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }
}

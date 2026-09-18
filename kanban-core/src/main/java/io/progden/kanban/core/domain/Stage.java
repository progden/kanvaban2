package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * Stage（階段）：{@code Board} 的內部實體，只能透過 {@link Board} 的方法異動（不是獨立 Aggregate）。
 *
 * <p>{@code stage.name} 欄位表沒有列出非空限制（與 {@link Swimlane} 不同），故不做空白檢查。
 */
public final class Stage {

    private final UUID id;
    private String name;
    private int order;
    private StageRole role;

    private Stage(UUID id, String name, int order, StageRole role) {
        this.id = id;
        this.name = resolveName(name);
        this.order = order;
        this.role = role;
    }

    static Stage create(String name, int order) {
        return new Stage(UUID.randomUUID(), name, order, StageRole.NONE);
    }

    static Stage reconstruct(UUID id, String name, int order, StageRole role) {
        return new Stage(id, name, order, role);
    }

    void rename(String newName) {
        this.name = resolveName(newName);
    }

    void reorder(int newOrder) {
        this.order = newOrder;
    }

    void changeRole(StageRole newRole) {
        this.role = newRole;
    }

    private static String resolveName(String name) {
        return name == null ? "" : name;
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

    public StageRole getRole() {
        return role;
    }
}

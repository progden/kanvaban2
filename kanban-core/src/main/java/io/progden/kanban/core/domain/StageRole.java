package io.progden.kanban.core.domain;

/**
 * Stage 在流程中的角色（CR-003）：同一個 {@code Board} 中 START、DONE 各至多一個，預設 NONE。
 */
public enum StageRole {
    NONE,
    START,
    DONE
}

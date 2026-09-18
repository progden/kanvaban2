package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 架構守門測試：kanban-core 不得依賴 Spring／JPA（見 CLAUDE.md「專案現況」）。
 * 若未來有任務不小心在 kanban-core 加了 Spring 依賴，這個測試會先失敗。
 */
class NoSpringDependencyTest {

    @Test
    void springFrameworkIsNotOnTheClasspath() {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("org.springframework.context.ApplicationContext"));
    }
}

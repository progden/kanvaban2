package io.progden.kanban.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 骨架煙霧測試：確認 Spring 容器能啟動、kanban-core 依賴能注入。
 * 自 T-01-be-user 起，資料庫改用 {@code src/test/resources/application.yml} 的 H2 設定，不再排除
 * DataSource／Hibernate 自動組態（見 `.dev/loops/implementation-loop/.state/decision-log.md`）。
 */
@SpringBootTest
class KanbanApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}

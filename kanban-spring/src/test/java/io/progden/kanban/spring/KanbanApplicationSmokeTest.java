package io.progden.kanban.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 骨架煙霧測試：確認 Spring 容器能啟動、kanban-core 依賴能注入。
 * 資料庫自動組態留給實際定義 persistence 的 Aggregate 任務（例如 T-02-be-board）處理，此處排除。
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class KanbanApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}

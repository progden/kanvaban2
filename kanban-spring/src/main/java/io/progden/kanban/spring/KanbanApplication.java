package io.progden.kanban.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * {@code io.progden.kanban.query}（跨 Aggregate 讀取投影，例如
 * {@link io.progden.kanban.query.BoardActivityLogQueryService}）是 CLAUDE.md 訂的獨立套件，
 * 不在 {@code io.progden.kanban.spring} 之下，{@code @SpringBootApplication} 預設的元件掃描
 * 不會掃到，需要另外列出（implementation-loop T-04 發現）。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.progden.kanban.spring", "io.progden.kanban.query"})
public class KanbanApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanbanApplication.class, args);
    }
}

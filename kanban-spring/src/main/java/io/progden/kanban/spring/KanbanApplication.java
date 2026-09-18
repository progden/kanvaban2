package io.progden.kanban.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * {@code @ComponentScan} 額外納入 {@code io.progden.kanban.query}：跨 aggregate 讀取投影放在該
 * 套件（CLAUDE.md「kanban-spring」說明），不在 {@code io.progden.kanban.spring} 預設掃描範圍下。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.progden.kanban.spring", "io.progden.kanban.query"})
public class KanbanApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanbanApplication.class, args);
    }
}

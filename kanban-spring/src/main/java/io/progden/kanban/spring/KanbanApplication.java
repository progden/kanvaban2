package io.progden.kanban.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code scanBasePackages} 用共同父套件 {@code io.progden.kanban}，涵蓋 {@code io.progden.kanban.spring}
 * 與跨 aggregate 讀取投影所在的 {@code io.progden.kanban.query}（CLAUDE.md「kanban-spring」說明）。
 * 不直接宣告 {@code @ComponentScan}：那會取代 {@code @SpringBootApplication} 內建的一份，連帶拿掉
 * Spring Boot 預設的 {@code TypeExcludeFilter}／{@code AutoConfigurationExcludeFilter}。
 */
@SpringBootApplication(scanBasePackages = "io.progden.kanban")
public class KanbanApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanbanApplication.class, args);
    }
}

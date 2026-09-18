package io.progden.kanban.core.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link Card#edit} 用的參數物件：對應 {@code uc-edit-card} post 可更新的欄位
 * （{@code card.description}／{@code card.due-date}／{@code card.labels}）。
 *
 * <p>不含負責人欄位：CR-002 已將負責人改為多選、參照看板成員，
 * 改由 F02「卡片負責人指派」情境處理，不在本 usecase 範圍內。
 */
public record CardDetails(String description, LocalDate dueDate, List<String> labels) {
}

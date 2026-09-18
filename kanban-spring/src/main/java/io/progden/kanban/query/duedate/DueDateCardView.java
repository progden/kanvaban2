package io.progden.kanban.query.duedate;

import java.time.LocalDate;
import java.util.UUID;

/** {@code uc-view-duedate-reminder} 清單中的一張卡片。 */
public record DueDateCardView(UUID cardId, String title, LocalDate dueDate) {
}

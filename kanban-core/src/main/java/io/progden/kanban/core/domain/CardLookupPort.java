package io.progden.kanban.core.domain;

import java.util.List;
import java.util.UUID;

/**
 * domain 層定義的介面（依賴反轉），讓 {@link Board} 可以查詢目前有哪些卡片、分別在哪個 Swimlane/Stage，
 * 藉此判斷刪除 Swimlane/Stage 時是否需要走確認/轉移流程；實際查詢邏輯由 kanban-spring 的
 * infrastructure 層實作（查詢 {@code Card} 的 repository）。
 */
public interface CardLookupPort {

    List<CardSummary> findByBoardId(UUID boardId);
}

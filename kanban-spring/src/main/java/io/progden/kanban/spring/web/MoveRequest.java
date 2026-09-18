package io.progden.kanban.spring.web;

import java.util.UUID;

/**
 * 拖曳調整 Swimlane／Stage 順序的請求 body；{@code beforeId} 為 null 時移到最後。
 */
record MoveRequest(UUID beforeId) {
}

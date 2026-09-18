package io.progden.kanban.spring.web;

import java.util.UUID;

/**
 * 新增 Stage 的請求 body；{@code beforeStageId} 為 null 時加到最後。
 */
record AddStageRequest(String name, UUID beforeStageId) {
}

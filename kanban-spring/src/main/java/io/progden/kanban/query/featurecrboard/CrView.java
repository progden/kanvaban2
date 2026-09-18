package io.progden.kanban.query.featurecrboard;

/**
 * 顯示在某個 Feature 底下的一筆 CR（「^CR-\d{3}$」標籤卡片）狀態。
 */
public record CrView(String crId, String status) {
}

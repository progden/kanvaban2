package io.progden.kanban.query.featurecrboard;

import java.util.List;

/**
 * 一個 Feature（「^F\d{2}$」標籤卡片）目前的狀態，以及顯示在其底下的 CR 清單（依 affects 標籤）。
 */
public record FeatureView(String featureId, String status, List<CrView> crs) {
}

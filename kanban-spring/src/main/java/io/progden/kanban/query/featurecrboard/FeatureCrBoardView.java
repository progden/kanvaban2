package io.progden.kanban.query.featurecrboard;

import java.util.List;

/**
 * {@code uc-view-feature-cr-board} 的計算結果：各 Feature 狀態（含其底下的 CR）、orphan CR 清單、
 * 標籤格式錯誤警告訊息（post p1～p4）。
 */
public record FeatureCrBoardView(List<FeatureView> features, List<String> orphanCrIds, List<String> warnings) {
}

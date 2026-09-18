package io.progden.kanban.query.featurecrboard;

import io.progden.kanban.core.domain.StageRole;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * spec-feature-cr-board.md {@code uc-view-feature-cr-board} 的計算邏輯：純函式，只依賴呼叫端
 * 傳入的卡片標籤與 Stage 角色，不接觸 persistence，方便單元測試（design-kanban-basic.md 同一原則）。
 *
 * <p>標籤格式（不分大小寫，見 post p5，內部一律轉大寫比對）：
 * Feature 卡「^F\d{2}$」、CR 卡「^CR-\d{3}$」、affects 標籤「^affects:F\d{2}$」（見「其他名詞」表）。
 *
 * <p>post p2「帶有 affects 標籤時，顯示在對應 Feature 底下」不要求該 Feature 編號真的有一張
 * Feature 卡：spec-feature-cr-board.md 範例情境（Scenario「檢視 CR 影響哪個 Feature 以及其狀態」）
 * 只給了一張 CR 卡，沒有對應的 F01 卡片，仍要求 CR 顯示在 Feature "F01" 底下——因此「對應 Feature」
 * 是由 affects 標籤的目標編號直接決定（沒有實體卡片時建立一個狀態預設「未開發」的佔位 Feature），
 * 與 post p3「orphan」是否成立分開判斷：orphan 只看該編號是否真的有一張 Feature 卡（見
 * implementation-loop decision-log，此為由範例情境推論出的實作細節）。
 *
 * <p>{@code post p4} 只針對「同一張卡片帶兩個 Feature 標籤」定義警告＋排除行為；CR 卡沒有 affects
 * 標籤、或同一張卡片帶兩個 CR 標籤時 spec 未定義，本計算器選擇：沒有 affects 標籤的 CR 卡不出現在
 * 任何清單中（不算 orphan——「orphan」定義明確要求 affects 指到不存在的 Feature 編號）；一張卡片帶
 * 多個 CR 標籤時逐一視為獨立 CR 處理（見 implementation-loop decision-log，此為低風險實作細節）。
 */
public final class FeatureCrBoardCalculator {

    private static final Pattern FEATURE_LABEL = Pattern.compile("^F\\d{2}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CR_LABEL = Pattern.compile("^CR-\\d{3}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern AFFECTS_LABEL = Pattern.compile("^affects:(F\\d{2})$", Pattern.CASE_INSENSITIVE);

    private FeatureCrBoardCalculator() {
    }

    public static FeatureCrBoardView calculate(List<CardLabelProjection> cards) {
        Map<String, FeatureEntry> featuresById = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        List<CrEntry> crCards = new ArrayList<>();

        for (CardLabelProjection card : cards) {
            List<String> featureLabels = matching(card.labels(), FEATURE_LABEL);
            if (featureLabels.size() > 1) {
                warnings.add("卡片「" + card.title() + "」帶有兩個 Feature 標籤，已忽略其 Feature／CR 統計");
            } else if (featureLabels.size() == 1) {
                String featureId = normalize(featureLabels.get(0));
                featuresById.put(featureId, new FeatureEntry(featureId, statusOf(card.stageRole())));
            }

            for (String crLabel : matching(card.labels(), CR_LABEL)) {
                String crId = normalize(crLabel);
                List<String> affectsTargets = matching(card.labels(), AFFECTS_LABEL).stream()
                        .map(FeatureCrBoardCalculator::extractAffectsTarget)
                        .map(FeatureCrBoardCalculator::normalize)
                        .toList();
                crCards.add(new CrEntry(crId, statusOf(card.stageRole()), affectsTargets));
            }
        }

        Set<String> featureIdsWithCard = Set.copyOf(featuresById.keySet());
        Map<String, List<CrView>> crsByFeature = new LinkedHashMap<>();
        List<String> orphanCrIds = new ArrayList<>();
        for (CrEntry cr : crCards) {
            for (String target : cr.affectsTargets()) {
                featuresById.putIfAbsent(target, new FeatureEntry(target, statusOf(StageRole.NONE)));
                crsByFeature.computeIfAbsent(target, k -> new ArrayList<>())
                        .add(new CrView(cr.crId(), cr.status()));
                if (!featureIdsWithCard.contains(target)) {
                    orphanCrIds.add(cr.crId());
                }
            }
        }

        List<FeatureView> features = featuresById.values().stream()
                .map(f -> new FeatureView(f.featureId(), f.status(),
                        crsByFeature.getOrDefault(f.featureId(), List.of())))
                .toList();

        return new FeatureCrBoardView(features, orphanCrIds, warnings);
    }

    private static List<String> matching(List<String> labels, Pattern pattern) {
        List<String> result = new ArrayList<>();
        for (String label : labels) {
            if (pattern.matcher(label).matches()) {
                result.add(label);
            }
        }
        return result;
    }

    private static String extractAffectsTarget(String affectsLabel) {
        var matcher = AFFECTS_LABEL.matcher(affectsLabel);
        if (!matcher.matches()) {
            throw new IllegalStateException("affects 標籤格式不符：" + affectsLabel);
        }
        return matcher.group(1);
    }

    private static String normalize(String label) {
        return label.toUpperCase(Locale.ROOT);
    }

    private static String statusOf(StageRole role) {
        return switch (role) {
            case DONE -> "已完成";
            case START -> "開發中";
            case NONE -> "未開發";
        };
    }

    private record FeatureEntry(String featureId, String status) {
    }

    private record CrEntry(String crId, String status, List<String> affectsTargets) {
    }
}

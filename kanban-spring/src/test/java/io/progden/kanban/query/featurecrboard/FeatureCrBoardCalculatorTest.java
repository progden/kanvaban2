package io.progden.kanban.query.featurecrboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.progden.kanban.core.domain.StageRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link FeatureCrBoardCalculator} 純函式單元測試，對應 spec-feature-cr-board.md
 * {@code uc-view-feature-cr-board} post p1～p5。
 */
class FeatureCrBoardCalculatorTest {

    @Test
    void featureCardStatusFollowsStageRole() {
        var cards = List.of(card("F01", StageRole.DONE));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertEquals(1, view.features().size());
        assertEquals("F01", view.features().get(0).featureId());
        assertEquals("已完成", view.features().get(0).status());
    }

    @Test
    void crCardShowsUnderAffectedFeature() {
        var cards = List.of(
                card("F01", StageRole.NONE),
                card(List.of("CR-004", "affects:F01"), StageRole.START));

        var view = FeatureCrBoardCalculator.calculate(cards);

        var feature = view.features().stream().filter(f -> f.featureId().equals("F01")).findFirst().orElseThrow();
        assertEquals(1, feature.crs().size());
        assertEquals("CR-004", feature.crs().get(0).crId());
        assertEquals("開發中", feature.crs().get(0).status());
    }

    @Test
    void crAffectingUnknownFeatureIsOrphan() {
        var cards = List.of(card(List.of("CR-099", "affects:F99"), StageRole.NONE));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertTrue(view.orphanCrIds().contains("CR-099"));
    }

    @Test
    void cardWithTwoFeatureLabelsProducesWarningAndIsExcluded() {
        var cards = List.of(card(List.of("F01", "F02"), StageRole.NONE));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertEquals(1, view.warnings().size());
        assertTrue(view.features().isEmpty());
    }

    @Test
    void crAffectingMultipleUnknownFeaturesIsOrphanOnlyOnce() {
        var cards = List.of(card(List.of("CR-099", "affects:F97", "affects:F98"), StageRole.NONE));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertEquals(List.of("CR-099"), view.orphanCrIds());
    }

    @Test
    void cardWithTwoFeatureLabelsDoesNotAffectOtherCards() {
        var cards = List.of(
                card(List.of("F01", "F02"), StageRole.NONE),
                card("F03", StageRole.DONE),
                card(List.of("CR-004", "affects:F03"), StageRole.START));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertEquals(1, view.warnings().size());
        var feature = view.features().stream().filter(f -> f.featureId().equals("F03")).findFirst().orElseThrow();
        assertEquals("已完成", feature.status());
        assertEquals(1, feature.crs().size());
        assertEquals("CR-004", feature.crs().get(0).crId());
        assertEquals("開發中", feature.crs().get(0).status());
        assertTrue(view.orphanCrIds().isEmpty());
    }

    @Test
    void labelMatchingIsCaseInsensitive() {
        var cards = List.of(card("f01", StageRole.NONE));

        var view = FeatureCrBoardCalculator.calculate(cards);

        assertEquals(1, view.features().size());
        assertEquals("F01", view.features().get(0).featureId());
    }

    private CardLabelProjection card(String label, StageRole role) {
        return card(List.of(label), role);
    }

    private CardLabelProjection card(List<String> labels, StageRole role) {
        return new CardLabelProjection(UUID.randomUUID(), "測試卡片", labels, role);
    }
}

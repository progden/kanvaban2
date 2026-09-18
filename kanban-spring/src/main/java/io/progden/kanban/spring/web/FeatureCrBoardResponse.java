package io.progden.kanban.spring.web;

import io.progden.kanban.query.featurecrboard.FeatureCrBoardView;
import java.util.List;

record FeatureCrBoardResponse(List<FeatureView> features, List<String> orphanCrIds, List<String> warnings) {

    static FeatureCrBoardResponse from(FeatureCrBoardView view) {
        List<FeatureView> featureViews = view.features().stream()
                .map(f -> new FeatureView(f.featureId(), f.status(),
                        f.crs().stream().map(cr -> new CrView(cr.crId(), cr.status())).toList()))
                .toList();
        return new FeatureCrBoardResponse(featureViews, view.orphanCrIds(), view.warnings());
    }

    record FeatureView(String featureId, String status, List<CrView> crs) {
    }

    record CrView(String crId, String status) {
    }
}

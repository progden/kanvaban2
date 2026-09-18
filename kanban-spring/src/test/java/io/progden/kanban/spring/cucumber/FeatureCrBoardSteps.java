package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.StageJpaEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-feature-cr-board.md「Feature: Feature／CR 追蹤表」對應的 Cucumber step definitions，透過
 * MockMvc 打 {@code FeatureCrBoardController} 的實際端點，驗證 web／query 整條路徑。
 *
 * <p>登入與開板、Stage 角色設定沿用 {@link BoardSteps} 既有的登入／建板／設定角色動作（直接呼叫其
 * 公開方法，而非重新實作一份），只有本 Feature 特有的「Background 合併成一句話」「建立帶標籤的卡片」
 * 「檢視追蹤表」步驟文字才在此類別新增。
 */
public class FeatureCrBoardSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardSteps boardSteps;

    private MvcResult lastResult;
    private Map<?, ?> lastBody;

    // ---- Given：Background ----

    @Given("我已登入系統，並開啟 Board {string}")
    public void givenLoggedInAndBoardOpened(String boardName) throws Exception {
        boardSteps.givenLoggedIn();
        boardSteps.givenBoardOpened(boardName);
    }

    @Given("Stage {string} 已設定角色為 {word}，Stage {string} 已設定角色為 {word}")
    public void givenStageRoles(String firstStage, String firstRole, String secondStage, String secondRole)
            throws Exception {
        boardSteps.whenSetStageRole(firstStage, firstRole.toUpperCase());
        boardSteps.whenSetStageRole(secondStage, secondRole.toUpperCase());
    }

    // ---- Given：帶標籤的卡片 ----

    @Given("卡片 {string} 標籤為 {string}，目前在角色為 {word} 的 Stage")
    public void givenCardWithLabelAtRoleStage(String title, String label, String role) throws Exception {
        createCardWithLabels(title, List.of(label), stageWithRole(role));
    }

    @Given("卡片 {string} 標籤為 {string}，並帶有 {string} 標籤，目前在角色為 {word} 的 Stage")
    public void givenCardWithTwoLabelsAtRoleStage(String title, String label, String affectsLabel, String role)
            throws Exception {
        createCardWithLabels(title, List.of(label, affectsLabel), stageWithRole(role));
    }

    @Given("卡片 {string} 標籤為 {string}，並帶有 {string} 標籤")
    public void givenCardWithTwoLabels(String title, String label, String affectsLabel) throws Exception {
        createCardWithLabels(title, List.of(label, affectsLabel), anyStageId());
    }

    @Given("卡片 {string} 同時帶有 {string} 與 {string} 兩個 Feature 標籤")
    public void givenCardWithTwoFeatureLabels(String title, String firstLabel, String secondLabel) throws Exception {
        createCardWithLabels(title, List.of(firstLabel, secondLabel), anyStageId());
    }

    @Given("卡片 {string} 標籤為 {string}")
    public void givenCardWithLabel(String title, String label) throws Exception {
        createCardWithLabels(title, List.of(label), anyStageId());
    }

    // ---- When ----

    @When("我開啟 Feature／CR 追蹤表")
    public void whenOpenFeatureCrBoard() throws Exception {
        lastResult = mockMvc.perform(get("/api/boards/" + boardSteps.getCurrentBoardId() + "/feature-cr-board")
                        .session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, lastResult.getResponse().getStatus(),
                "檢視 Feature／CR 追蹤表失敗：" + lastResult.getResponse().getContentAsString());
        lastBody = objectMapper.readValue(lastResult.getResponse().getContentAsString(), Map.class);
    }

    // ---- Then ----

    @Then("Feature {string} 的狀態應該顯示為「{word}」")
    public void thenFeatureStatusIs(String featureId, String status) {
        assertEquals(status, findFeature(featureId).get("status"));
    }

    @Then("Feature {string} 底下應該顯示一筆狀態為「{word}」的 CR {string}")
    public void thenFeatureHasCrWithStatus(String featureId, String status, String crId) {
        Map<?, ?> feature = findFeature(featureId);
        List<?> crs = (List<?>) feature.get("crs");
        boolean found = crs.stream().anyMatch(cr -> {
            Map<?, ?> crMap = (Map<?, ?>) cr;
            return crId.equals(crMap.get("crId")) && status.equals(crMap.get("status"));
        });
        assertTrue(found, "Feature " + featureId + " 底下找不到狀態為「" + status + "」的 CR " + crId);
    }

    @Then("{string} 應該出現在 orphan CR 清單中")
    public void thenOrphanCrListContains(String crId) {
        List<?> orphanCrIds = (List<?>) lastBody.get("orphanCrIds");
        assertTrue(orphanCrIds.contains(crId));
    }

    @Then("應該顯示一筆警告訊息，說明該卡片有兩個 Feature 標籤")
    public void thenWarningAboutTwoFeatureLabels() {
        List<?> warnings = (List<?>) lastBody.get("warnings");
        assertEquals(1, warnings.size());
    }

    /**
     * 這個 Scenario 的 Background 只建立了一張卡片（同時帶 F01／F02 兩個 Feature 標籤），沒有第二張
     * 「其他卡片」可以在這裡驗證是否受影響；這一步只能驗到「這張卡片自己的 F01／F02 沒有出現在追蹤
     * 表」。「其他正常卡片的 Feature／CR 統計不受這張錯誤卡片影響」由
     * {@link io.progden.kanban.query.featurecrboard.FeatureCrBoardCalculatorTest
     * #cardWithTwoFeatureLabelsDoesNotAffectOtherCards()} 驗證。
     */
    @Then("其他卡片的 Feature／CR 統計不應該受影響")
    public void thenOtherCardsUnaffected() {
        List<?> features = (List<?>) lastBody.get("features");
        assertFalse(features.stream().anyMatch(f -> "F01".equals(((Map<?, ?>) f).get("featureId"))
                || "F02".equals(((Map<?, ?>) f).get("featureId"))));
    }

    @Then("應該視同標籤為 {string} 顯示")
    public void thenTreatedAsLabel(String featureId) {
        assertEquals("未開發", findFeature(featureId).get("status"));
    }

    // ---- helpers ----

    private Map<?, ?> findFeature(String featureId) {
        List<?> features = (List<?>) lastBody.get("features");
        return features.stream()
                .map(f -> (Map<?, ?>) f)
                .filter(f -> featureId.equals(f.get("featureId")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("追蹤表中找不到 Feature " + featureId));
    }

    private UUID stageWithRole(String role) {
        BoardJpaEntity board = boardSteps.loadBoardEntity();
        return board.getStages().stream()
                .filter(s -> s.getRole().name().equals(role.toUpperCase()))
                .map(StageJpaEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到角色為 " + role + " 的 Stage"));
    }

    private UUID anyStageId() {
        return boardSteps.loadBoardEntity().getStages().get(0).getId();
    }

    private void createCardWithLabels(String title, List<String> labels, UUID stageId) throws Exception {
        UUID swimlaneId = boardSteps.loadBoardEntity().getSwimlanes().get(0).getId();
        Map<String, String> createBody = new HashMap<>();
        createBody.put("title", title);
        createBody.put("swimlaneId", swimlaneId.toString());
        createBody.put("stageId", stageId.toString());
        MvcResult createResult = mockMvc.perform(post("/api/boards/" + boardSteps.getCurrentBoardId() + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn();
        assertEquals(201, createResult.getResponse().getStatus(),
                "測試前置資料建立失敗：" + createResult.getResponse().getContentAsString());
        Map<?, ?> createdBody = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        UUID cardId = UUID.fromString((String) createdBody.get("id"));

        Map<String, Object> editBody = new HashMap<>();
        editBody.put("description", null);
        editBody.put("dueDate", null);
        editBody.put("labels", labels);
        MvcResult editResult = mockMvc.perform(patch("/api/cards/" + cardId)
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editBody)))
                .andReturn();
        assertEquals(200, editResult.getResponse().getStatus(),
                "測試前置資料設定標籤失敗：" + editResult.getResponse().getContentAsString());
    }
}

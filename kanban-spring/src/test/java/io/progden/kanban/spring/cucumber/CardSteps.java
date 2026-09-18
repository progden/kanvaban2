package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import io.progden.kanban.spring.persistence.CardActivityRecordJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-kanban-basic.md「Card（卡片）編輯」對應的 Cucumber step definitions，透過 MockMvc 打
 * {@code CardController} 的實際端點，驗證 web／application／persistence 整條路徑。
 *
 * <p>「我確認新增」「我確認刪除」「該操作應該被記錄為一筆活動紀錄」三段步驟文字與 {@link BoardSteps}
 * 共用，實際 dispatch 在 {@link BoardSteps}，本類別只提供被呼叫的 {@code submitPendingAddCard}／
 * {@code confirmDeleteCard}／{@code assertCardActivityRecorded}（見 {@link CrossAggregateState}）。
 */
public class CardSteps {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardJpaRepository cardJpaRepository;

    @Autowired
    private CrossAggregateState crossState;

    @Autowired
    private BoardSteps boardSteps;

    @Autowired
    private UserSteps userSteps;

    private MvcResult lastResult;
    private UUID currentCardId;
    private String currentCardTitle;
    private long cardCountBeforeAttempt;
    private long cardActivityCountBefore;
    private String pendingDescription;
    private LocalDate pendingDueDate;
    private List<String> pendingLabels;
    private UUID stageIdBeforeMove;

    // ---- Given：Background ----

    @Given("看板中存在 Swimlane {string} 與 Stage {string}、{string}、{string}")
    public void givenBoardHasSwimlaneAndStages(String swimlaneName, String s1, String s2, String s3) throws Exception {
        boardSteps.ensureSwimlaneExists(swimlaneName);
        assertEquals(List.of(s1, s2, s3), boardSteps.loadBoardEntity().getStages().stream()
                .map(io.progden.kanban.spring.persistence.StageJpaEntity::getName).toList());
    }

    @Given("看板中還有另一個 Swimlane {string}")
    public void givenAnotherSwimlaneExists(String name) throws Exception {
        boardSteps.ensureSwimlaneExists(name);
    }

    @Given("存在一張卡片 {string}")
    public void givenCardExists(String title) throws Exception {
        createCard(title, "本週優先", "待辦");
    }

    @Given("存在一張卡片 {string}，位於 Swimlane {string} 與 Stage {string}")
    public void givenCardExistsAt(String title, String swimlaneName, String stageName) throws Exception {
        createCard(title, swimlaneName, stageName);
    }

    @Given("存在一張卡片 {string}，位於 Stage {string}")
    public void givenCardExistsAtStage(String title, String stageName) throws Exception {
        createCard(title, "本週優先", stageName);
    }

    // ---- When：新增卡片 ----

    @When("我在 Swimlane {string} 的 Stage {string} 欄位點擊「新增卡片」")
    public void whenClickAddCard(String swimlaneName, String stageName) {
        UUID swimlaneId = boardSteps.resolveSwimlaneId(swimlaneName);
        UUID stageId = boardSteps.resolveStageId(stageName);
        crossState.beginAddCard(swimlaneId, stageId);
    }

    @When("我輸入標題 {string}")
    public void whenEnterCardTitle(String title) {
        crossState.setPendingCardTitle(title);
    }

    /**
     * 由 {@link BoardSteps} 的「我確認新增」dispatcher 呼叫（見 {@link CrossAggregateState}）。
     */
    void submitPendingAddCard() throws Exception {
        UUID swimlaneId = crossState.pendingCardSwimlaneId();
        UUID stageId = crossState.pendingCardStageId();
        String title = crossState.pendingCardTitle();
        cardActivityCountBefore = 0;
        lastResult = attemptCreate(title, swimlaneId, stageId);
        syncLastResult();
        if (lastResult.getResponse().getStatus() == 201) {
            Map<?, ?> body = readBody(lastResult);
            currentCardId = UUID.fromString((String) body.get("id"));
            currentCardTitle = title;
            crossState.lastActivityOnCard(currentCardId);
        }
        crossState.clearAddCard();
    }

    @When("我嘗試新增一張標題為空的卡片")
    public void whenAttemptAddCardWithBlankTitle() throws Exception {
        UUID swimlaneId = boardSteps.resolveSwimlaneId("本週優先");
        UUID stageId = boardSteps.resolveStageId("待辦");
        cardCountBeforeAttempt = activeCardCount();
        lastResult = attemptCreate("", swimlaneId, stageId);
        syncLastResult();
    }

    // ---- When：編輯卡片 ----

    @When("我開啟該卡片的詳細編輯畫面")
    public void whenOpenCardDetail() {
        // 純畫面導覽，後端無對應行為；進入本步驟代表指定的 card 已存在（uc-edit-card pre p1）。
    }

    @When("我填寫以下欄位：")
    public void whenFillEditFields(DataTable table) {
        pendingDescription = null;
        pendingDueDate = null;
        pendingLabels = null;
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            String field = row.get("欄位");
            String value = row.get("內容");
            switch (field) {
                case "描述" -> pendingDescription = value;
                case "截止日期" -> pendingDueDate = LocalDate.parse(value);
                case "標籤" -> pendingLabels = splitLabels(value);
                default -> throw new IllegalArgumentException("未知欄位：" + field);
            }
        }
    }

    @When("我儲存變更")
    public void whenSaveCardChanges() throws Exception {
        captureCardActivityBaseline(currentCardId);
        Map<String, Object> body = new HashMap<>();
        body.put("description", pendingDescription);
        body.put("dueDate", pendingDueDate == null ? null : pendingDueDate.toString());
        body.put("labels", pendingLabels);
        lastResult = mockMvc.perform(patch("/api/cards/" + currentCardId)
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    // ---- When：移動卡片 ----

    @When("我將該卡片拖曳到 Swimlane {string} 的 Stage {string}")
    public void whenDragCardToSwimlane(String swimlaneName, String stageName) throws Exception {
        UUID swimlaneId = boardSteps.resolveSwimlaneId(swimlaneName);
        captureCardActivityBaseline(currentCardId);
        Map<String, String> body = Map.of("swimlaneId", swimlaneId.toString());
        lastResult = mockMvc.perform(post("/api/cards/" + currentCardId + "/move-swimlane")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    @When("我將該卡片拖曳到 Stage {string}")
    public void whenDragCardToStage(String stageName) throws Exception {
        UUID stageId = boardSteps.resolveStageId(stageName);
        stageIdBeforeMove = loadCurrentCard().getStageId();
        Map<String, String> body = Map.of("stageId", stageId.toString());
        lastResult = mockMvc.perform(post("/api/cards/" + currentCardId + "/move-stage")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    // ---- When：留言 ----

    @When("我在卡片中新增留言 {string}")
    public void whenAddComment(String content) throws Exception {
        Map<String, String> body = Map.of("content", content);
        lastResult = mockMvc.perform(post("/api/cards/" + currentCardId + "/comments")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    // ---- When：刪除卡片 ----

    @When("我點擊刪除該卡片")
    public void whenClickDeleteCard() {
        crossState.pendingDeleteCard(currentCardId);
    }

    @When("我在確認訊息中選擇取消")
    public void whenCancelDeleteCard() {
        crossState.clearPendingDeleteCard();
    }

    /**
     * 由 {@link BoardSteps} 的「我確認刪除」dispatcher 呼叫（見 {@link CrossAggregateState}）。
     */
    void confirmDeleteCard(UUID cardId) throws Exception {
        captureCardActivityBaseline(cardId);
        lastResult = mockMvc.perform(delete("/api/cards/" + cardId).session(boardSteps.getSession())).andReturn();
        syncLastResult();
    }

    // ---- Then：新增卡片 ----

    @Then("該卡片應該出現在 Swimlane {string} 與 Stage {string} 的交會格中")
    public void thenCardAppearsAt(String swimlaneName, String stageName) {
        CardJpaEntity card = loadCurrentCard();
        assertEquals(boardSteps.resolveSwimlaneId(swimlaneName), card.getSwimlaneId());
        assertEquals(boardSteps.resolveStageId(stageName), card.getStageId());
    }

    @Then("卡片標題應該顯示為 {string}")
    public void thenCardTitleIs(String title) {
        assertEquals(title, loadCurrentCard().getTitle());
    }

    @Then("不應該建立新的卡片")
    public void thenNoNewCardCreated() {
        assertEquals(cardCountBeforeAttempt, activeCardCount());
    }

    // ---- Then：編輯卡片 ----

    @Then("卡片應該保存上述所有欄位的內容")
    public void thenCardFieldsSaved() {
        CardJpaEntity card = loadCurrentCard();
        assertEquals(pendingDescription, card.getDescription());
        assertEquals(pendingDueDate, card.getDueDate());
        assertEquals(pendingLabels, card.getLabels());
    }

    @Then("卡片縮圖應該顯示截止日期 {string}")
    public void thenCardThumbnailShowsDueDate(String dueDate) {
        assertEquals(LocalDate.parse(dueDate), loadCurrentCard().getDueDate());
    }

    // ---- Then：移動卡片 ----

    @Then("該卡片應該顯示於 Swimlane {string} 與 Stage {string} 的交會格中")
    public void thenCardShownAt(String swimlaneName, String stageName) {
        CardJpaEntity card = loadCurrentCard();
        assertEquals(boardSteps.resolveSwimlaneId(swimlaneName), card.getSwimlaneId());
        assertEquals(boardSteps.resolveStageId(stageName), card.getStageId());
    }

    @Then("該卡片不應該再出現在 Swimlane {string} 中")
    public void thenCardNotInSwimlane(String swimlaneName) {
        assertFalse(loadCurrentCard().getSwimlaneId().equals(boardSteps.resolveSwimlaneId(swimlaneName)));
    }

    @Then("該卡片應該顯示於 Stage {string}")
    public void thenCardShownAtStage(String stageName) {
        assertEquals(boardSteps.resolveStageId(stageName), loadCurrentCard().getStageId());
    }

    @Then("卡片的狀態異動應該被記錄，包含操作人、異動時間與異動前後的 Stage")
    public void thenStageTransitionRecorded() {
        CardJpaEntity card = loadCurrentCard();
        assertFalse(card.getStageTransitions().isEmpty());
        var transition = card.getStageTransitions().get(card.getStageTransitions().size() - 1);
        assertEquals(boardSteps.getCurrentUserId(), transition.getOperatorId());
        assertEquals(stageIdBeforeMove, transition.getFromStageId());
        assertEquals(card.getStageId(), transition.getToStageId());
        assertTrue(transition.getOccurredAt().isBefore(Instant.now().plusSeconds(1)));
    }

    // ---- Then：留言 ----

    @Then("該留言應該顯示在卡片的留言列表中")
    public void thenCommentListed() {
        assertFalse(loadCurrentCard().getComments().isEmpty());
    }

    @Then("留言應該記錄留言者與留言時間")
    public void thenCommentRecordsAuthorAndTime() {
        CardJpaEntity card = loadCurrentCard();
        var comment = card.getComments().get(card.getComments().size() - 1);
        assertEquals(boardSteps.getCurrentUserId(), comment.getAuthorId());
        assertTrue(comment.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    // ---- Then：刪除卡片 ----

    @Then("系統應該顯示確認訊息 {string}")
    public void thenDeleteConfirmationMessageShown(String message) {
        // 確認訊息為前端靜態文案，uc-delete-card 除 p2 取消分支外無其他失敗定義，
        // 後端無對應行為，這一步只標記情境已進入確認階段（見 ui-kanban-basic.md s-card-delete-dialog）。
    }

    @Then("該卡片應該從看板中移除")
    public void thenCardRemovedFromBoard() {
        assertEquals(204, lastResult.getResponse().getStatus());
        assertTrue(cardJpaRepository.findById(currentCardId).orElseThrow().isDeleted());
    }

    @Then("該卡片應該仍然存在於看板中")
    public void thenCardStillExists() {
        assertFalse(cardJpaRepository.findById(currentCardId).orElseThrow().isDeleted());
    }

    // ---- 供 BoardSteps dispatcher 呼叫 ----

    void assertCardActivityRecorded() {
        UUID cardId = crossState.lastCardActivityId();
        List<CardActivityRecordJpaEntity> activityLog = cardJpaRepository.findById(cardId).orElseThrow()
                .getActivityLog();
        assertEquals(cardActivityCountBefore + 1, activityLog.size());
        CardActivityRecordJpaEntity latest = activityLog.stream()
                .max((a, b) -> a.getOccurredAt().compareTo(b.getOccurredAt()))
                .orElseThrow();
        assertEquals(boardSteps.getCurrentUserId(), latest.getOperatorId());
        assertTrue(latest.getOccurredAt().isBefore(Instant.now().plusSeconds(1)));
    }

    // ---- helpers ----

    private void captureCardActivityBaseline(UUID cardId) {
        cardActivityCountBefore = cardJpaRepository.findById(cardId).orElseThrow().getActivityLog().size();
        crossState.lastActivityOnCard(cardId);
    }

    private void createCard(String title, String swimlaneName, String stageName) throws Exception {
        UUID swimlaneId = boardSteps.resolveSwimlaneId(swimlaneName);
        UUID stageId = boardSteps.resolveStageId(stageName);
        MvcResult result = attemptCreate(title, swimlaneId, stageId);
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
        Map<?, ?> body = readBody(result);
        currentCardId = UUID.fromString((String) body.get("id"));
        currentCardTitle = title;
    }

    private MvcResult attemptCreate(String title, UUID swimlaneId, UUID stageId) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("swimlaneId", swimlaneId.toString());
        body.put("stageId", stageId.toString());
        return mockMvc.perform(post("/api/boards/" + boardSteps.getCurrentBoardId() + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    /**
     * 供 {@link BoardClockSteps} 重用「不應該建立新的卡片」這段共用步驟文字：
     * 在它自己的「嘗試建立卡片」動作前先記錄基準值，比照 {@link UserSteps#setLastResult} 的重用模式。
     */
    void captureCardCountBaseline() {
        cardCountBeforeAttempt = activeCardCount();
    }

    /** 供 {@link BoardClockSteps} 依 id 查卡片（board-clock Scenario 不追蹤 {@code currentCardId}）。 */
    CardJpaEntity loadCardById(UUID id) {
        return cardJpaRepository.findById(id).orElseThrow();
    }

    private long activeCardCount() {
        return cardJpaRepository.findByBoardIdAndDeletedFalse(boardSteps.getCurrentBoardId()).size();
    }

    private CardJpaEntity loadCurrentCard() {
        return cardJpaRepository.findById(currentCardId).orElseThrow();
    }

    private List<String> splitLabels(String value) {
        List<String> labels = new ArrayList<>();
        for (String label : value.split(",")) {
            labels.add(label.trim());
        }
        return labels;
    }

    private void syncLastResult() {
        userSteps.setLastResult(lastResult);
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}

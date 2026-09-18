package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.ActivityRecordJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.StageJpaEntity;
import io.progden.kanban.spring.persistence.SwimlaneJpaEntity;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」對應的 Cucumber step definitions，
 * 透過 MockMvc 打 {@code BoardController} 的實際端點，驗證 web／application／persistence 整條路徑。
 *
 * <p>Card Aggregate（T-03）尚未實作，「刪除包含卡片的 Swimlane/Stage」相關情境改用
 * {@link FakeCardLookupPort} 模擬卡片資料與「卡片已被移除／轉移」的效果，驗證 Board 端的
 * 刪除保護機制；真正的卡片刪除／搬移由 T-03/T-04 接上（見 implementation-loop 交接摘要）。
 */
public class BoardSteps {

    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private FakeCardLookupPort fakeCardLookupPort;

    @Autowired
    private UserSteps userSteps;

    private MockHttpSession session;
    private MvcResult lastResult;
    private UUID currentUserId;
    private UUID currentBoardId;
    private String pendingName;
    private UUID pendingBeforeStageId;
    private String currentSwimlaneName;
    private String currentStageName;
    private int lastCardCount;
    private int activityCountBeforeAction;
    private String expectedActivityKeyword;

    @Before
    public void resetBoardState() {
        boardJpaRepository.deleteAll();
        fakeCardLookupPort.reset();
        session = new MockHttpSession();
        lastResult = null;
        pendingName = null;
        pendingBeforeStageId = null;
        activityCountBeforeAction = 0;
        expectedActivityKeyword = null;
    }

    // ---- Given：登入與開板 ----

    @Given("我已登入系統")
    public void givenLoggedIn() throws Exception {
        String username = "board-owner-" + UUID.randomUUID();
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", DEFAULT_PASSWORD);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, loginResult.getResponse().getStatus());
        currentUserId = userJpaRepository.findByUsername(username).orElseThrow().getId();
    }

    @Given("我已開啟一個名為 {string} 的看板")
    public void givenBoardOpened(String name) throws Exception {
        Map<String, String> body = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
        Map<?, ?> responseBody = readBody(result);
        currentBoardId = UUID.fromString((String) responseBody.get("id"));
    }

    // ---- Given：Swimlane 前置狀態 ----

    @Given("看板目前有 {int} 個 Swimlane {string}")
    public void givenBoardHasSwimlaneCount(int count, String name) {
        BoardJpaEntity board = loadBoardEntity();
        assertEquals(count, board.getSwimlanes().size());
        assertEquals(name, board.getSwimlanes().get(0).getName());
        currentSwimlaneName = name;
    }

    @Given("我正在新增一個 Swimlane")
    public void givenAboutToAddSwimlane() {
        pendingName = null;
    }

    @Given("看板中存在一個 Swimlane {string}")
    public void givenSwimlaneExists(String name) throws Exception {
        ensureSwimlaneExists(name);
        currentSwimlaneName = name;
    }

    @Given("看板中依序存在 Swimlane {string}、{string}、{string}")
    public void givenSwimlanesInOrder(String first, String second, String third) throws Exception {
        BoardJpaEntity board = loadBoardEntity();
        UUID firstId = board.getSwimlanes().get(0).getId();
        renameSwimlane(firstId, first);
        addSwimlane(second);
        addSwimlane(third);
    }

    @Given("看板中存在一個沒有任何卡片的 Swimlane {string}")
    public void givenEmptySwimlaneExists(String name) throws Exception {
        ensureSwimlaneExists(name);
        currentSwimlaneName = name;
    }

    @Given("看板中存在一個 Swimlane {string}，其中包含 {int} 張卡片")
    public void givenSwimlaneWithCards(String name, int cardCount) throws Exception {
        ensureSwimlaneExists(name);
        currentSwimlaneName = name;
        lastCardCount = cardCount;
        UUID swimlaneId = resolveSwimlaneId(name);
        UUID anyStageId = loadBoardEntity().getStages().get(0).getId();
        for (int i = 0; i < cardCount; i++) {
            fakeCardLookupPort.addCard(currentBoardId, swimlaneId, anyStageId);
        }
    }

    @Given("看板中只剩下 {int} 個 Swimlane {string}")
    public void givenOnlyOneSwimlaneLeft(int count, String name) {
        BoardJpaEntity board = loadBoardEntity();
        assertEquals(count, board.getSwimlanes().size());
        assertEquals(name, board.getSwimlanes().get(0).getName());
        currentSwimlaneName = name;
    }

    // ---- Given：Stage 前置狀態 ----

    @Given("看板目前的 Stage 依序為 {string}、{string}、{string}")
    public void givenStagesInOrder(String first, String second, String third) {
        BoardJpaEntity board = loadBoardEntity();
        assertEquals(List.of(first, second, third),
                board.getStages().stream().map(StageJpaEntity::getName).toList());
    }

    @Given("Stage {string} 目前沒有任何卡片")
    public void givenStageHasNoCards(String name) throws Exception {
        ensureStageExists(name);
        currentStageName = name;
    }

    @Given("Stage {string} 中包含 {int} 張卡片")
    public void givenStageWithCards(String name, int cardCount) throws Exception {
        ensureStageExists(name);
        currentStageName = name;
        lastCardCount = cardCount;
        UUID stageId = resolveStageId(name);
        UUID anySwimlaneId = loadBoardEntity().getSwimlanes().get(0).getId();
        for (int i = 0; i < cardCount; i++) {
            fakeCardLookupPort.addCard(currentBoardId, anySwimlaneId, stageId);
        }
    }

    @Given("看板中只剩下 {int} 個 Stage {string}")
    public void givenOnlyOneStageLeft(int count, String name) throws Exception {
        BoardJpaEntity board = loadBoardEntity();
        List<StageJpaEntity> others = board.getStages().stream()
                .filter(s -> !s.getName().equals(name))
                .toList();
        for (StageJpaEntity other : others) {
            removeStage(other.getId());
        }
        board = loadBoardEntity();
        assertEquals(count, board.getStages().size());
        currentStageName = name;
    }

    @Given("看板目前所有 Stage 的角色皆為 NONE")
    public void givenAllStageRolesAreNone() {
        BoardJpaEntity board = loadBoardEntity();
        assertTrue(board.getStages().stream().allMatch(s -> s.getRole().name().equals("NONE")));
    }

    // ---- When：Swimlane ----

    @When("我點擊「新增 Swimlane」按鈕")
    public void whenClickAddSwimlaneButton() {
        pendingName = null;
    }

    @When("我輸入名稱 {string}")
    public void whenEnterName(String name) {
        pendingName = name;
    }

    @When("我確認新增")
    public void whenConfirmAddSwimlane() throws Exception {
        doAddSwimlane(pendingName == null ? "" : pendingName);
    }

    @When("我沒有輸入任何名稱就確認新增")
    public void whenConfirmAddSwimlaneWithoutName() throws Exception {
        doAddSwimlane("");
    }

    @When("我將該 Swimlane 重新命名為 {string}")
    public void whenRenameCurrentSwimlane(String newName) throws Exception {
        UUID swimlaneId = resolveSwimlaneId(currentSwimlaneName);
        renameSwimlane(swimlaneId, newName);
        currentSwimlaneName = newName;
    }

    @When("我將 Swimlane {string} 拖曳到 {string} 的上方")
    public void whenDragSwimlaneAbove(String dragged, String anchor) throws Exception {
        UUID draggedId = resolveSwimlaneId(dragged);
        UUID anchorId = resolveSwimlaneId(anchor);
        captureActivityBaseline("調整 Swimlane 順序");
        Map<String, String> body = Map.of("beforeId", anchorId.toString());
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/swimlanes/" + draggedId + "/move")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    @When("我刪除該 Swimlane")
    public void whenDeleteCurrentSwimlane() throws Exception {
        UUID swimlaneId = resolveSwimlaneId(currentSwimlaneName);
        deleteSwimlane(swimlaneId);
    }

    @When("我嘗試刪除該 Swimlane")
    public void whenAttemptDeleteCurrentSwimlane() throws Exception {
        UUID swimlaneId = resolveSwimlaneId(currentSwimlaneName);
        deleteSwimlane(swimlaneId);
    }

    @When("我確認刪除")
    public void whenConfirmDelete() throws Exception {
        // 替身警告（見 OQ-IMPL-15）：正式 BoardController 的刪除端點沒有「已確認」輸入，
        // 有卡片時只會回 409；這裡直接操作 fakeCardLookupPort 模擬「應用層協調完成後卡片已清空」
        // 的結果，不是打正式端點驗證 uc-delete-swimlane post 第 2 條，等 OQ-IMPL-15 定案由對應任務
        // 補上協調流程後，要改成透過正式端點傳「已確認」再驗證。
        UUID swimlaneId = resolveSwimlaneId(currentSwimlaneName);
        fakeCardLookupPort.removeAllCardsInSwimlane(swimlaneId);
        deleteSwimlane(swimlaneId);
    }

    // ---- When：Stage ----

    @When("我點擊「新增 Stage」按鈕")
    public void whenClickAddStageButton() {
        pendingName = null;
        pendingBeforeStageId = null;
    }

    @When("我選擇插入在 {string} 與 {string} 之間")
    public void whenChooseInsertPosition(String before, String after) throws Exception {
        pendingBeforeStageId = resolveStageId(after);
        doAddStage(pendingName, pendingBeforeStageId);
    }

    @When("我沒有指定插入位置就確認新增")
    public void whenConfirmAddStageAtEnd() throws Exception {
        doAddStage(pendingName, null);
    }

    @When("我將 Stage {string} 重新命名為 {string}")
    public void whenRenameStage(String currentName, String newName) throws Exception {
        UUID stageId = resolveStageId(currentName);
        renameStage(stageId, newName);
    }

    @When("我將 Stage {string} 拖曳到 {string} 的左側")
    public void whenDragStageLeft(String dragged, String anchor) throws Exception {
        UUID draggedId = resolveStageId(dragged);
        UUID anchorId = resolveStageId(anchor);
        captureActivityBaseline("調整 Stage 順序");
        Map<String, String> body = Map.of("beforeId", anchorId.toString());
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/stages/" + draggedId + "/move")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    @When("我刪除 Stage {string}")
    public void whenDeleteStage(String name) throws Exception {
        UUID stageId = resolveStageId(name);
        deleteStage(stageId);
    }

    @When("我嘗試刪除 Stage {string}")
    public void whenAttemptDeleteStage(String name) throws Exception {
        currentStageName = name;
        UUID stageId = resolveStageId(name);
        deleteStage(stageId);
    }

    @When("我選擇目的 Stage 為 {string}")
    public void whenChooseDestinationStage(String destinationName) throws Exception {
        // 替身警告（見 OQ-IMPL-15）：正式 BoardController 的刪除端點沒有「目的 Stage」輸入，
        // 有卡片時只會回 409；這裡直接操作 fakeCardLookupPort 模擬「應用層協調完成後卡片已轉移」
        // 的結果，不是打正式端點驗證 uc-delete-stage post 第 2 條，等 OQ-IMPL-15 定案由對應任務
        // 補上協調流程後，要改成透過正式端點傳目的 Stage 再驗證。
        UUID sourceId = resolveStageId(currentStageName);
        UUID destinationId = resolveStageId(destinationName);
        fakeCardLookupPort.moveAllCardsToStage(sourceId, destinationId);
        deleteStage(sourceId);
    }

    @When("我嘗試刪除該 Stage")
    public void whenAttemptDeleteCurrentStage() throws Exception {
        UUID stageId = resolveStageId(currentStageName);
        deleteStage(stageId);
    }

    @When("我將 Stage {string} 的角色設定為 {word}")
    public void whenSetStageRole(String name, String role) throws Exception {
        UUID stageId = resolveStageId(name);
        captureActivityBaseline("設定 Stage");
        Map<String, String> body = Map.of("role", role);
        lastResult = mockMvc.perform(patch("/api/boards/" + currentBoardId + "/stages/" + stageId + "/role")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    // ---- Then：Swimlane ----

    @Then("看板應該顯示 {int} 個 Swimlane")
    public void thenBoardShowsSwimlaneCount(int count) {
        assertEquals(count, loadBoardEntity().getSwimlanes().size());
    }

    @Then("新的 Swimlane {string} 應該出現在看板最下方")
    public void thenNewSwimlaneAtBottom(String name) {
        List<SwimlaneJpaEntity> swimlanes = loadBoardEntity().getSwimlanes();
        assertEquals(name, swimlanes.get(swimlanes.size() - 1).getName());
    }

    @Then("該 Swimlane 的名稱應該更新為 {string}")
    public void thenSwimlaneNameUpdated(String name) {
        assertTrue(loadBoardEntity().getSwimlanes().stream().anyMatch(s -> s.getName().equals(name)));
    }

    @Then("Swimlane 的順序應該變為 {string}、{string}、{string}")
    public void thenSwimlaneOrderIs(String first, String second, String third) {
        assertEquals(List.of(first, second, third),
                loadBoardEntity().getSwimlanes().stream().map(SwimlaneJpaEntity::getName).toList());
    }

    @Then("不應該建立新的 Swimlane")
    public void thenNoNewSwimlaneCreated() {
        assertEquals(1, loadBoardEntity().getSwimlanes().size());
        assertEquals(activityCountBeforeAction, loadBoardEntity().getActivityLog().size());
    }

    @Then("看板不應該再顯示 {string}")
    public void thenBoardNoLongerShows(String name) {
        BoardJpaEntity board = loadBoardEntity();
        assertFalse(board.getSwimlanes().stream().anyMatch(s -> s.getName().equals(name)));
        assertFalse(board.getStages().stream().anyMatch(s -> s.getName().equals(name)));
    }

    @Then("系統應該顯示確認訊息，告知該 Swimlane 內有 {int} 張卡片將一併被刪除")
    public void thenSwimlaneDeleteConfirmationShown(int cardCount) throws Exception {
        assertEquals(409, lastResult.getResponse().getStatus());
        assertTrue(readBody(lastResult).get("message").toString().contains(String.valueOf(cardCount)));
    }

    @Then("該 Swimlane 與其所有卡片都應該被移除")
    public void thenSwimlaneAndCardsRemoved() {
        // 替身警告（見 OQ-IMPL-15）：這裡只驗證 Swimlane 本身不存在，沒有驗證卡片是否真的被刪除
        // ——Card Aggregate（T-03）尚未實作，卡片刪除是 whenConfirmDelete 直接操作
        // fakeCardLookupPort 模擬出來的，不是正式程式碼路徑的行為，「一併被刪除」這條 post 尚未被驗證。
        assertEquals(204, lastResult.getResponse().getStatus());
        assertFalse(loadBoardEntity().getSwimlanes().stream()
                .anyMatch(s -> s.getName().equals(currentSwimlaneName)));
    }

    @Then("該 Swimlane 不應該被刪除")
    public void thenSwimlaneNotDeleted() {
        assertTrue(loadBoardEntity().getSwimlanes().stream().anyMatch(s -> s.getName().equals(currentSwimlaneName)));
        assertEquals(activityCountBeforeAction, loadBoardEntity().getActivityLog().size());
    }

    // ---- Then：Stage ----

    @Then("Stage 順序應該變為 {string}、{string}、{string}、{string}")
    public void thenStageOrderIsFour(String first, String second, String third, String fourth) {
        assertEquals(List.of(first, second, third, fourth),
                loadBoardEntity().getStages().stream().map(StageJpaEntity::getName).toList());
    }

    @Then("Stage 順序應該變為 {string}、{string}、{string}")
    public void thenStageOrderIsThree(String first, String second, String third) {
        assertEquals(List.of(first, second, third),
                loadBoardEntity().getStages().stream().map(StageJpaEntity::getName).toList());
    }

    @Then("新的 Stage {string} 應該出現在最後一個欄位")
    public void thenNewStageAtEnd(String name) {
        List<StageJpaEntity> stages = loadBoardEntity().getStages();
        assertEquals(name, stages.get(stages.size() - 1).getName());
    }

    @Then("Stage 名稱應該更新為 {string}")
    public void thenStageNameUpdated(String name) {
        assertTrue(loadBoardEntity().getStages().stream().anyMatch(s -> s.getName().equals(name)));
    }

    @Then("該 Stage 不應該被刪除")
    public void thenStageNotDeleted() {
        assertTrue(loadBoardEntity().getStages().stream().anyMatch(s -> s.getName().equals(currentStageName)));
        assertEquals(activityCountBeforeAction, loadBoardEntity().getActivityLog().size());
    }

    @Then("系統應該提示我選擇一個目的 Stage 來接收這 {int} 張卡片")
    public void thenPromptForDestinationStage(int cardCount) throws Exception {
        assertEquals(409, lastResult.getResponse().getStatus());
        assertTrue(readBody(lastResult).get("message").toString().contains(String.valueOf(cardCount)));
    }

    @Then("這 {int} 張卡片應該被移動到 {string}")
    public void thenCardsMovedTo(int cardCount, String destinationName) {
        // 替身警告（見 OQ-IMPL-15）：這裡讀的是 whenChooseDestinationStage 自己寫入
        // fakeCardLookupPort 的結果，驗證的是測試替身自己做的事，不是正式端點／應用層的行為
        // ——「card.stage 更新為使用者選擇的目的 stage」這條 post 尚未被正式程式碼路徑驗證。
        UUID destinationId = resolveStageId(destinationName);
        long moved = fakeCardLookupPort.findByBoardId(currentBoardId).stream()
                .filter(c -> c.stageId().equals(destinationId))
                .count();
        assertEquals(cardCount, moved);
    }

    @Then("Stage {string} 應該被刪除")
    public void thenStageDeleted(String name) {
        assertEquals(204, lastResult.getResponse().getStatus());
        assertFalse(loadBoardEntity().getStages().stream().anyMatch(s -> s.getName().equals(name)));
    }

    @Then("Stage {string} 的角色應該變為 {word}")
    public void thenStageRoleIs(String name, String role) {
        StageJpaEntity stage = loadBoardEntity().getStages().stream()
                .filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
        assertEquals(role, stage.getRole().name());
    }

    @Then("Stage {string} 的角色應該自動變回 NONE")
    public void thenStageRoleResetToNone(String name) {
        StageJpaEntity stage = loadBoardEntity().getStages().stream()
                .filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
        assertEquals("NONE", stage.getRole().name());
    }

    // ---- Then：共用（活動紀錄） ----

    @Then("該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間")
    public void thenActivityRecorded() {
        BoardJpaEntity board = loadBoardEntity();
        List<ActivityRecordJpaEntity> activityLog = board.getActivityLog();
        assertEquals(activityCountBeforeAction + 1, activityLog.size());
        ActivityRecordJpaEntity latest = activityLog.stream()
                .max((a, b) -> a.getOccurredAt().compareTo(b.getOccurredAt()))
                .orElseThrow();
        assertEquals(currentUserId, latest.getOperatorId());
        assertTrue(latest.getOccurredAt().isBefore(Instant.now().plusSeconds(1)));
        if (expectedActivityKeyword != null) {
            assertTrue(latest.getAction().contains(expectedActivityKeyword),
                    "活動紀錄內容應包含「" + expectedActivityKeyword + "」，實際為：" + latest.getAction());
        }
    }

    // ---- helpers ----

    private void captureActivityBaseline(String expectedKeyword) {
        activityCountBeforeAction = loadBoardEntity().getActivityLog().size();
        expectedActivityKeyword = expectedKeyword;
    }

    private void doAddSwimlane(String name) throws Exception {
        captureActivityBaseline("新增 Swimlane");
        Map<String, String> body = Map.of("name", name);
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/swimlanes")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void addSwimlane(String name) throws Exception {
        doAddSwimlane(name);
    }

    private void renameSwimlane(UUID swimlaneId, String newName) throws Exception {
        captureActivityBaseline("重新命名 Swimlane");
        Map<String, String> body = Map.of("name", newName);
        lastResult = mockMvc.perform(patch("/api/boards/" + currentBoardId + "/swimlanes/" + swimlaneId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void deleteSwimlane(UUID swimlaneId) throws Exception {
        captureActivityBaseline("刪除 Swimlane");
        lastResult = mockMvc.perform(delete("/api/boards/" + currentBoardId + "/swimlanes/" + swimlaneId)
                        .session(session))
                .andReturn();
        syncLastResult();
    }

    private void ensureSwimlaneExists(String name) throws Exception {
        if (resolveSwimlaneIdOptional(name) == null) {
            doAddSwimlane(name);
        }
    }

    private void doAddStage(String name, UUID beforeStageId) throws Exception {
        captureActivityBaseline("新增 Stage");
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("beforeStageId", beforeStageId == null ? null : beforeStageId.toString());
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/stages")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void renameStage(UUID stageId, String newName) throws Exception {
        captureActivityBaseline("重新命名 Stage");
        Map<String, String> body = Map.of("name", newName);
        lastResult = mockMvc.perform(patch("/api/boards/" + currentBoardId + "/stages/" + stageId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void deleteStage(UUID stageId) throws Exception {
        captureActivityBaseline("刪除 Stage");
        lastResult = mockMvc.perform(delete("/api/boards/" + currentBoardId + "/stages/" + stageId)
                        .session(session))
                .andReturn();
        syncLastResult();
    }

    private void removeStage(UUID stageId) throws Exception {
        deleteStage(stageId);
    }

    private void ensureStageExists(String name) throws Exception {
        if (resolveStageIdOptional(name) == null) {
            doAddStage(name, null);
        }
    }

    private BoardJpaEntity loadBoardEntity() {
        return boardJpaRepository.findById(currentBoardId).orElseThrow();
    }

    private UUID resolveSwimlaneId(String name) {
        UUID id = resolveSwimlaneIdOptional(name);
        if (id == null) {
            throw new IllegalStateException("找不到 Swimlane：" + name);
        }
        return id;
    }

    private UUID resolveSwimlaneIdOptional(String name) {
        return loadBoardEntity().getSwimlanes().stream()
                .filter(s -> s.getName().equals(name))
                .map(SwimlaneJpaEntity::getId)
                .findFirst().orElse(null);
    }

    private UUID resolveStageId(String name) {
        UUID id = resolveStageIdOptional(name);
        if (id == null) {
            throw new IllegalStateException("找不到 Stage：" + name);
        }
        return id;
    }

    private UUID resolveStageIdOptional(String name) {
        return loadBoardEntity().getStages().stream()
                .filter(s -> s.getName().equals(name))
                .map(StageJpaEntity::getId)
                .findFirst().orElse(null);
    }

    private void syncLastResult() {
        userSteps.setLastResult(lastResult);
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}

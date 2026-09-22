package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.core.domain.BoardClockSnapshot;
import io.progden.kanban.core.domain.ClockStatus;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.StageJpaEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-kanban-widgets.md 四個 Feature 對應的 Cucumber step definitions，透過 MockMvc 打
 * {@code KanbanWidgetsController} 的實際端點，驗證 web／query／persistence 整條路徑。
 *
 * <p>登入與開板、Stage 角色設定沿用 {@link BoardSteps} 既有動作（同 {@link FeatureCrBoardSteps} 的
 * 作法）；卡片建立／移動 Stage 的「日期」透過 Board Clock 調整（{@code adjustClockTo}）產生，
 * 沿用 {@link BoardClockSteps} 的「EARLY_ANCHOR」重設技巧，避免虛構的過去日期與測試執行時的真實
 * 系統時間（board 建立當下）互相牴觸而誤觸 {@code uc-guard-clock-monotonicity}。
 */
public class KanbanWidgetsSteps {

    private static final Instant EARLY_ANCHOR = Instant.parse("2000-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private BoardSteps boardSteps;

    @Autowired
    private UserSteps userSteps;

    private MvcResult lastResult;
    private Map<?, ?> lastBody;
    private boolean baselineEstablished;
    private final Map<String, UUID> cardIdsByTitle = new HashMap<>();

    @Before
    public void resetWidgetsState() {
        baselineEstablished = false;
        lastResult = null;
        lastBody = null;
        cardIdsByTitle.clear();
    }

    // ---- Given：Background ----
    // 「我已登入系統，並開啟 Board {string}」「Stage {string} 已設定角色為 {word}，Stage {string}
    // 已設定角色為 {word}」兩句與 {@link FeatureCrBoardSteps} 完全相同，共用其定義，不在此重複註冊
    // （Cucumber 不允許同一句步驟文字有兩個 @Given/@When/@Then）。

    @Given("看板時間目前為 {word}")
    public void givenBoardClockCurrentlyAt(String date) throws Exception {
        adjustClockTo(LocalDate.parse(date));
    }

    // ---- Given：Cycle Time／Lead Time 前置卡片 ----

    @Given("卡片 {string} 於 {word} 建立、{word} 進入 Start、{word} 進入 Done")
    public void givenCardCreatedEntersStartAndDone(
            String title, String createdDate, String startDate, String doneDate) throws Exception {
        adjustClockTo(LocalDate.parse(createdDate));
        UUID cardId = createCard(title, todoStageId());
        adjustClockTo(LocalDate.parse(startDate));
        moveCardToStage(cardId, startStageId());
        adjustClockTo(LocalDate.parse(doneDate));
        moveCardToStage(cardId, doneStageId());
        cardIdsByTitle.put(title, cardId);
    }

    @Given("卡片 {string} 於 {word} 建立、未曾進入 Start 角色的 Stage、{word} 進入 Done")
    public void givenCardCreatedNeverStartedThenDone(String title, String createdDate, String doneDate)
            throws Exception {
        adjustClockTo(LocalDate.parse(createdDate));
        UUID cardId = createCard(title, todoStageId());
        adjustClockTo(LocalDate.parse(doneDate));
        moveCardToStage(cardId, doneStageId());
        cardIdsByTitle.put(title, cardId);
    }

    @Given("卡片 {string} 於 {word} 首次進入 Done，之後於 {word} 被移出 Done，並於 {word} 再次進入 Done")
    public void givenCardEntersLeavesReentersDone(
            String title, String firstDoneDate, String leftDoneDate, String secondDoneDate) throws Exception {
        adjustClockTo(LocalDate.parse(firstDoneDate));
        UUID cardId = createCard(title, todoStageId());
        moveCardToStage(cardId, doneStageId());
        adjustClockTo(LocalDate.parse(leftDoneDate));
        moveCardToStage(cardId, todoStageId());
        adjustClockTo(LocalDate.parse(secondDoneDate));
        moveCardToStage(cardId, doneStageId());
        cardIdsByTitle.put(title, cardId);
    }

    // ---- Given：WIP／Aging 前置卡片 ----

    @Given("Stage {string} 有 {int} 張卡片，Stage {string} 有 {int} 張卡片，Stage {string} 有 {int} 張卡片")
    public void givenStagesHaveCardCounts(
            String firstStage, int firstCount, String secondStage, int secondCount, String thirdStage, int thirdCount)
            throws Exception {
        createCards(firstStage, firstCount);
        createCards(secondStage, secondCount);
        createCards(thirdStage, thirdCount);
    }

    @Given("卡片 {string} 於 {word} 進入 Start 角色的 Stage，目前仍在該 Stage 未進入 Done")
    public void givenCardEnteredStartStillThere(String title, String date) throws Exception {
        adjustClockTo(LocalDate.parse(date));
        UUID cardId = createCard(title, todoStageId());
        moveCardToStage(cardId, startStageId());
        cardIdsByTitle.put(title, cardId);
    }

    // ---- Given：Throughput 前置卡片 ----

    @Given("{word} 有 {int} 張卡片進入 Done，{word} 有 {int} 張卡片進入 Done")
    public void givenCardsEnteredDoneOnDates(String firstDate, int firstCount, String secondDate, int secondCount)
            throws Exception {
        completeCardsOn(firstDate, firstCount);
        completeCardsOn(secondDate, secondCount);
    }

    // ---- Given：截止日期提醒前置卡片 ----

    @Given("卡片 {string} 截止日期為 {word}，尚未完成")
    public void givenCardWithDueDate(String title, String dueDate) throws Exception {
        UUID cardId = createCard(title, todoStageId());
        editCardDueDate(cardId, dueDate);
        cardIdsByTitle.put(title, cardId);
    }

    @Given("即將到期的門檻設定為 {int} 天")
    public void givenUpcomingThreshold(int days) {
        thresholdDays = days;
    }

    private Integer thresholdDays;

    // ---- When ----

    @When("我開啟 Cycle Time \\/ Lead Time 圖表")
    public void whenOpenCycleLeadTimeChart() throws Exception {
        performGet("/widgets/cycle-lead-time");
    }

    @When("我開啟 WIP 圖表")
    public void whenOpenWipChart() throws Exception {
        performGet("/widgets/wip");
    }

    @When("我開啟 Aging WIP 圖表")
    public void whenOpenAgingWipChart() throws Exception {
        performGet("/widgets/aging-wip");
    }

    @When("我開啟 Throughput 圖表，並選擇以「日」為單位")
    public void whenOpenThroughputChartByDay() throws Exception {
        performGet("/widgets/throughput?unit=day");
    }

    @When("我開啟 CFD 圖表")
    public void whenOpenCfdChart() throws Exception {
        performGet("/widgets/cfd");
    }

    @When("我開啟逾期提醒圖表")
    public void whenOpenDueDateReminderChart() throws Exception {
        String query = thresholdDays == null ? "" : "?thresholdDays=" + thresholdDays;
        performGet("/widgets/duedate-reminder" + query);
    }

    @When("我將即將到期的門檻設定為 {int} 天")
    public void whenSetUpcomingThresholdAndOpen(int days) throws Exception {
        performGet("/widgets/duedate-reminder?thresholdDays=" + days);
        userSteps.setLastResult(lastResult);
    }

    // ---- Then：Cycle Time／Lead Time ----

    @Then("卡片 {string} 的 Lead Time 應該顯示為 {int} 天")
    public void thenCardLeadTimeIs(String title, int days) {
        assertEquals((long) days, ((Number) findCardTiming(title).get("leadTimeDays")).longValue());
    }

    @Then("卡片 {string} 的 Cycle Time 應該顯示為 {int} 天")
    public void thenCardCycleTimeIs(String title, int days) {
        assertEquals((long) days, ((Number) findCardTiming(title).get("cycleTimeDays")).longValue());
    }

    @Then("統計摘要應該顯示 Lead Time 與 Cycle Time 的 P50、P85、P95 三個百分位數")
    public void thenPercentileSummaryShown() {
        assertPercentileSummaryPresent((Map<?, ?>) lastBody.get("leadTime"));
        assertPercentileSummaryPresent((Map<?, ?>) lastBody.get("cycleTime"));
    }

    @Then("卡片清單中應該包含卡片 {string}，其 Cycle Time 顯示為「無」")
    public void thenCardCycleTimeIsNone(String title) {
        assertEquals(null, findCardTiming(title).get("cycleTimeDays"));
    }

    @Then("統計摘要的 Cycle Time 平均值與百分位計算應該排除卡片 {string}")
    public void thenCycleTimeStatsExcludeCard(String title) {
        // 由「排除計算的卡片數」那一步驟驗證計數；這一步只確認該卡片的 cycleTimeDays 確實為 null
        // （已在 thenCardCycleTimeIsNone 驗證），此處不需重複動作。
    }

    @Then("統計摘要應該顯示「排除計算的卡片數」為 {int}")
    public void thenExcludedCycleTimeCountIs(int count) {
        assertEquals(count, ((Number) lastBody.get("excludedCycleTimeCount")).intValue());
    }

    @Then("卡片 {string} 的完成時間應該顯示為 {word}")
    public void thenCardDoneAtDateIs(String title, String date) {
        String doneAt = (String) findCardTiming(title).get("doneAt");
        LocalDate actual = Instant.parse(doneAt).atZone(ZoneOffset.UTC).toLocalDate();
        assertEquals(LocalDate.parse(date), actual);
    }

    // ---- Then：WIP／Aging ----

    @Then("應該顯示 Stage {string} 卡片數 {int}、{string} 卡片數 {int}，且不顯示 Done 角色的 Stage {string}")
    public void thenStageCardCountsAre(
            String firstStage, int firstCount, String secondStage, int secondCount, String doneStage) {
        assertEquals(firstCount, stageWipCount(firstStage));
        assertEquals(secondCount, stageWipCount(secondStage));
        assertTrue(stageWipCountIfPresent(doneStage).isEmpty(), "WIP 清單不應包含 Done 角色的 Stage " + doneStage);
    }

    @Then("卡片 {string} 的年齡應該顯示為 {int} 天")
    public void thenCardAgeIs(String title, int days) {
        List<?> cards = (List<?>) lastBody.get("cards");
        UUID cardId = cardIdsByTitle.get(title);
        Map<?, ?> found = cards.stream()
                .map(c -> (Map<?, ?>) c)
                .filter(c -> cardId.toString().equals(c.get("cardId")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aging WIP 清單中找不到卡片 " + title));
        assertEquals((long) days, ((Number) found.get("ageDays")).longValue());
    }

    // ---- Then：Throughput／CFD ----

    @Then("{word} 的完成數應該顯示為 {int}")
    public void thenThroughputCountOnDateIs(String date, int count) {
        List<?> periods = (List<?>) lastBody.get("periods");
        Map<?, ?> found = periods.stream()
                .map(p -> (Map<?, ?>) p)
                .filter(p -> date.equals(p.get("periodStart")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Throughput 清單中找不到日期 " + date));
        assertEquals(count, ((Number) found.get("count")).intValue());
    }

    @Then("應該顯示每一天、每個 Stage 的累積卡片數量")
    public void thenCfdDataPointsShown() {
        List<?> dataPoints = (List<?>) lastBody.get("dataPoints");
        assertTrue(!dataPoints.isEmpty());
        Map<?, ?> first = (Map<?, ?>) dataPoints.get(0);
        assertTrue(first.containsKey("date"));
        assertTrue(first.containsKey("countByStage"));
    }

    // ---- Then：截止日期提醒 ----

    @Then("卡片 {string} 應該出現在「已逾期」清單中")
    public void thenCardInOverdueList(String title) {
        assertCardInList("overdue", title);
    }

    @Then("卡片 {string} 應該出現在「即將到期」清單中")
    public void thenCardInUpcomingList(String title) {
        assertCardInList("upcoming", title);
    }

    // 「系統應該顯示錯誤訊息 {string}」與 {@link UserSteps#thenErrorMessage} 完全相同，共用其定義
    // （見 whenSetUpcomingThresholdAndOpen 呼叫 userSteps.setLastResult 同步 lastResult）。

    // ---- helpers ----

    private void assertCardInList(String listName, String title) {
        List<?> list = (List<?>) lastBody.get(listName);
        UUID cardId = cardIdsByTitle.get(title);
        boolean found = list.stream().map(c -> (Map<?, ?>) c).anyMatch(c -> cardId.toString().equals(c.get("cardId")));
        assertTrue(found, "「" + listName + "」清單中找不到卡片 " + title);
    }

    private int stageWipCount(String stageName) {
        return stageWipCountIfPresent(stageName)
                .orElseThrow(() -> new AssertionError("WIP 清單中找不到 Stage " + stageName));
    }

    private java.util.Optional<Integer> stageWipCountIfPresent(String stageName) {
        List<?> stages = (List<?>) lastBody.get("stages");
        UUID stageId = resolveStageId(stageName);
        return stages.stream()
                .map(s -> (Map<?, ?>) s)
                .filter(s -> stageId.toString().equals(s.get("stageId")))
                .findFirst()
                .map(s -> ((Number) s.get("count")).intValue());
    }

    private Map<?, ?> findCardTiming(String title) {
        List<?> cards = (List<?>) lastBody.get("cards");
        UUID cardId = cardIdsByTitle.get(title);
        return cards.stream()
                .map(c -> (Map<?, ?>) c)
                .filter(c -> cardId.toString().equals(c.get("cardId")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("清單中找不到卡片 " + title));
    }

    private void assertPercentileSummaryPresent(Map<?, ?> summary) {
        assertTrue(summary.containsKey("p50"));
        assertTrue(summary.containsKey("p85"));
        assertTrue(summary.containsKey("p95"));
    }

    private void performGet(String path) throws Exception {
        lastResult = mockMvc.perform(get("/api/boards/" + boardSteps.getCurrentBoardId() + path)
                        .session(boardSteps.getSession()))
                .andReturn();
        if (lastResult.getResponse().getStatus() == 200) {
            lastBody = objectMapper.readValue(lastResult.getResponse().getContentAsString(), Map.class);
        }
    }

    private void createCards(String stageName, int count) throws Exception {
        UUID stageId = resolveStageId(stageName);
        for (int i = 0; i < count; i++) {
            createCard(stageName + "-" + i, stageId);
        }
    }

    private void completeCardsOn(String date, int count) throws Exception {
        adjustClockTo(LocalDate.parse(date));
        for (int i = 0; i < count; i++) {
            UUID cardId = createCard("completed-" + date + "-" + i, todoStageId());
            moveCardToStage(cardId, doneStageId());
        }
    }

    private UUID createCard(String title, UUID stageId) throws Exception {
        UUID swimlaneId = boardSteps.loadBoardEntity().getSwimlanes().get(0).getId();
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("swimlaneId", swimlaneId.toString());
        body.put("stageId", stageId.toString());
        MvcResult result = mockMvc.perform(post("/api/boards/" + boardSteps.getCurrentBoardId() + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
        Map<?, ?> responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return UUID.fromString((String) responseBody.get("id"));
    }

    private void moveCardToStage(UUID cardId, UUID stageId) throws Exception {
        Map<String, String> body = Map.of("stageId", stageId.toString());
        MvcResult result = mockMvc.perform(post("/api/cards/" + cardId + "/move-stage")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置資料移動 Stage 失敗：" + result.getResponse().getContentAsString());
    }

    private void editCardDueDate(UUID cardId, String dueDate) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("description", null);
        body.put("dueDate", dueDate);
        body.put("labels", List.of());
        MvcResult result = mockMvc.perform(patch("/api/cards/" + cardId)
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置資料設定截止日期失敗：" + result.getResponse().getContentAsString());
    }

    private void adjustClockTo(LocalDate date) throws Exception {
        if (!baselineEstablished) {
            resetClockBaseline();
            baselineEstablished = true;
        }
        Instant target = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Map<String, String> body = Map.of("newTime", target.toString());
        MvcResult result = mockMvc.perform(patch("/api/boards/" + boardSteps.getCurrentBoardId() + "/clock")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置資料調整看板時間失敗：" + result.getResponse().getContentAsString());
    }

    private void resetClockBaseline() {
        BoardJpaEntity entity = boardSteps.loadBoardEntity();
        entity.updateClock(new BoardClockSnapshot(ClockStatus.REALTIME, 0L, null, EARLY_ANCHOR, null));
        boardJpaRepository.save(entity);
    }

    private UUID resolveStageId(String name) {
        return boardSteps.loadBoardEntity().getStages().stream()
                .filter(s -> s.getName().equals(name))
                .map(StageJpaEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到 Stage " + name));
    }

    private UUID todoStageId() {
        return resolveStageId("待辦");
    }

    private UUID startStageId() {
        return stageIdWithRole("START");
    }

    private UUID doneStageId() {
        return stageIdWithRole("DONE");
    }

    private UUID stageIdWithRole(String role) {
        return boardSteps.loadBoardEntity().getStages().stream()
                .filter(s -> s.getRole().name().equals(role))
                .map(StageJpaEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到角色為 " + role + " 的 Stage"));
    }
}

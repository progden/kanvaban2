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
import io.progden.kanban.spring.persistence.ActivityRecordJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-board-clock.md「Feature: 看板時間管理」對應的 Cucumber step definitions，透過 MockMvc
 * 打 {@code BoardController} 的看板時鐘端點，驗證 web／application／persistence 整條路徑。
 *
 * <p>「看板時間目前為 {word} {word}」在 Background 第一次出現時，除了呼叫正式的
 * {@code adjustClock} 端點外，還會先把這個測試用 board 的 {@code lastEventAt} 基準直接重設到一個
 * 很早的錨點（{@link #EARLY_ANCHOR}）：{@code Board.create} 的第一筆「建立看板」活動一定是用真正的
 * 系統時間（呼叫端傳入），而本檔的 Scenario 全部使用 2026-09-12 這組虛構日期，早於測試實際執行的
 * 系統時間，兩者混在一起會讓 {@code uc-guard-clock-monotonicity} 誤判「看板剛建立的真實時間」也是
 * 一筆擋在前面的事件。這個重設只在每個 Scenario 的 Background 做一次（用 {@link #baselineEstablished}
 * 旗標避免同一段步驟文字在 Scenario 內文重複出現時，把已經記錄好的 {@code lastEventAt} 誤蓋掉，
 * 見「看板時間早於最後一筆事件時，不可建立新事件」那個 Scenario），之後同一個 Scenario 內的所有調整
 * 都走正式的 {@code adjustClock}／建卡片端點，不再繞過 guard。
 */
public class BoardClockSteps {

    private static final Instant EARLY_ANCHOR = Instant.parse("2000-01-01T00:00:00Z");
    private static final DateTimeFormatter NARRATIVE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Duration TOLERANCE = Duration.ofSeconds(5);
    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private BoardSteps boardSteps;

    @Autowired
    private CardSteps cardSteps;

    @Autowired
    private UserSteps userSteps;

    private MvcResult lastResult;
    private MockHttpSession nonOwnerSession;
    private boolean baselineEstablished;
    private Instant lastKnownClockTime;
    private int activityCountBeforeAction;
    private UUID lastCreatedCardId;
    private String lastCreatedCardTitle;

    @Before
    public void resetClockState() {
        baselineEstablished = false;
        nonOwnerSession = null;
        lastResult = null;
        lastKnownClockTime = null;
        lastCreatedCardId = null;
        lastCreatedCardTitle = null;
    }

    // ---- Given：Background ----

    @Given("我已登入系統，並開啟 Board {string}")
    public void givenLoggedInAndBoardOpened(String name) throws Exception {
        boardSteps.givenLoggedIn();
        boardSteps.givenBoardOpened(name);
    }

    @Given("看板時間目前為 {word} {word}")
    public void givenClockCurrentlyAt(String date, String time) throws Exception {
        Instant target = parseNarrative(date, time);
        if (!baselineEstablished) {
            resetClockBaseline();
            baselineEstablished = true;
        }
        lastResult = performAdjustClock(boardSteps.getSession(), target);
        assertEquals(200, lastResult.getResponse().getStatus());
        lastKnownClockTime = target;
    }

    @Given("看板中最後一筆事件發生於 {word} {word}")
    public void givenLastEventOccurredAt(String date, String time) throws Exception {
        Instant target = parseNarrative(date, time);
        MvcResult adjustResult = performAdjustClock(boardSteps.getSession(), target);
        assertEquals(200, adjustResult.getResponse().getStatus());
        lastKnownClockTime = target;
        MvcResult createResult = createCard("基準事件");
        assertEquals(201, createResult.getResponse().getStatus(),
                "測試前置資料建立失敗：" + createResult.getResponse().getContentAsString());
    }

    @Given("看板時間目前為暫停狀態，暫停時的時間為 {word} {word}")
    public void givenClockPausedAt(String date, String time) throws Exception {
        Instant target = parseNarrative(date, time);
        MvcResult adjustResult = performAdjustClock(boardSteps.getSession(), target);
        assertEquals(200, adjustResult.getResponse().getStatus());
        MvcResult pauseResult = mockMvc.perform(post(clockUrl() + "/pause").session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, pauseResult.getResponse().getStatus());
        lastKnownClockTime = target;
    }

    @Given("我是該 Board 的 Owner")
    public void givenIAmOwner() {
        // 看板建立者依現行慣例即為 Owner（見 BoardApplicationService 類別註解），不需額外動作。
    }

    @Given("我不是該 Board 的 Owner")
    public void givenIAmNotOwner() throws Exception {
        nonOwnerSession = loginAsNewUser();
    }

    // ---- When ----

    @When("我建立一張卡片 {string}")
    public void whenCreateCard(String title) throws Exception {
        lastResult = createCard(title);
        assertEquals(201, lastResult.getResponse().getStatus(),
                "建立卡片失敗：" + lastResult.getResponse().getContentAsString());
        Map<?, ?> body = readBody(lastResult);
        lastCreatedCardId = UUID.fromString((String) body.get("id"));
        lastCreatedCardTitle = title;
        syncLastResult();
    }

    @When("我嘗試建立一張卡片 {string}")
    public void whenAttemptCreateCard(String title) throws Exception {
        cardSteps.captureCardCountBaseline();
        lastResult = createCard(title);
        syncLastResult();
    }

    @When("我將看板時間調整為 {word} {word}")
    public void whenAdjustClock(String date, String time) throws Exception {
        Instant target = parseNarrative(date, time);
        captureActivityBaseline();
        lastResult = performAdjustClock(boardSteps.getSession(), target);
        assertEquals(200, lastResult.getResponse().getStatus());
        lastKnownClockTime = target;
        syncLastResult();
    }

    @When("我嘗試將看板時間調整為 {word} {word}")
    public void whenAttemptAdjustClock(String date, String time) throws Exception {
        Instant target = parseNarrative(date, time);
        lastResult = performAdjustClock(nonOwnerSession, target);
        syncLastResult();
    }

    @When("我暫停看板時間")
    public void whenPauseClock() throws Exception {
        captureActivityBaseline();
        lastResult = mockMvc.perform(post(clockUrl() + "/pause").session(boardSteps.getSession())).andReturn();
        assertEquals(200, lastResult.getResponse().getStatus());
        syncLastResult();
    }

    @When("我恢復看板時間")
    public void whenResumeClock() throws Exception {
        captureActivityBaseline();
        lastResult = mockMvc.perform(post(clockUrl() + "/resume").session(boardSteps.getSession())).andReturn();
        assertEquals(200, lastResult.getResponse().getStatus());
        syncLastResult();
    }

    @When("我等待 {int} 秒")
    public void whenWaitSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
    }

    // ---- Then ----

    @Then("卡片 {string} 的建立時間應該是 {word} {word}")
    public void thenCardCreatedAt(String title, String date, String time) {
        assertEquals(lastCreatedCardTitle, title);
        Instant expected = parseNarrative(date, time);
        Instant actual = cardSteps.loadCardById(lastCreatedCardId).getActivityLog().stream()
                .min((a, b) -> a.getOccurredAt().compareTo(b.getOccurredAt()))
                .orElseThrow()
                .getOccurredAt();
        assertCloseTo(expected, actual);
    }

    @Then("看板時間應該顯示 {word} {word}")
    public void thenClockDisplays(String date, String time) throws Exception {
        Instant expected = parseNarrative(date, time);
        assertCloseTo(expected, currentClockTime());
    }

    @Then("看板時間應該仍顯示 {word} {word}")
    public void thenClockStillDisplays(String date, String time) throws Exception {
        thenClockDisplays(date, time);
    }

    @Then("看板時間應該從 {word} {word} 繼續隨系統時間前進")
    public void thenClockAdvancesFrom(String date, String time) throws Exception {
        Instant baseline = parseNarrative(date, time);
        Instant actual = currentClockTime();
        assertTrue(!actual.isBefore(baseline), "看板時間應該從 " + baseline + " 開始，實際為 " + actual);
        assertTrue(actual.isBefore(baseline.plus(TOLERANCE)),
                "看板時間應該持續隨系統時間前進，實際為 " + actual);
        assertEquals(ClockStatus.REALTIME.name(), currentClockStatus());
    }

    @Then("看板時間應該維持不變")
    public void thenClockUnchanged() throws Exception {
        assertCloseTo(lastKnownClockTime, currentClockTime());
    }

    @Then("應該新增一筆活動紀錄，說明看板時間被調整為 {word} {word}")
    public void thenActivityRecordedForAdjust(String date, String time) {
        Instant expected = parseNarrative(date, time);
        var latest = assertActivityRecorded("調整看板時間");
        assertCloseTo(expected, latest.getOccurredAt());
        assertEquals(boardSteps.getCurrentUserId(), latest.getOperatorId());
    }

    @Then("應該新增一筆活動紀錄，說明看板時間已暫停")
    public void thenActivityRecordedForPause() {
        var latest = assertActivityRecorded("暫停看板時間");
        assertEquals(boardSteps.getCurrentUserId(), latest.getOperatorId());
    }

    @Then("應該新增一筆活動紀錄，說明看板時間已恢復")
    public void thenActivityRecordedForResume() {
        var latest = assertActivityRecorded("恢復看板時間");
        assertEquals(boardSteps.getCurrentUserId(), latest.getOperatorId());
    }

    // ---- helpers ----

    private ActivityRecordJpaEntity assertActivityRecorded(String expectedKeyword) {
        var activityLog = boardSteps.loadBoardEntity().getActivityLog();
        assertEquals(activityCountBeforeAction + 1, activityLog.size());
        // 用插入順序取「最新一筆」而非 max(occurredAt)：看板時鐘可能被調到比真實建立時間更早的
        // 虛構過去，occurredAt 不再保證等於插入順序（design-board-clock.md 決議紀錄，PAUSED 排序說明）。
        var latest = activityLog.get(activityLog.size() - 1);
        assertTrue(latest.getAction().contains(expectedKeyword),
                "活動紀錄內容應包含「" + expectedKeyword + "」，實際為：" + latest.getAction());
        return latest;
    }

    private void captureActivityBaseline() {
        activityCountBeforeAction = boardSteps.loadBoardEntity().getActivityLog().size();
    }

    private void resetClockBaseline() {
        BoardJpaEntity entity = boardSteps.loadBoardEntity();
        entity.updateClock(new BoardClockSnapshot(ClockStatus.REALTIME, 0L, null, EARLY_ANCHOR, null));
        boardJpaRepository.save(entity);
    }

    private MvcResult performAdjustClock(MockHttpSession session, Instant newTime) throws Exception {
        Map<String, String> body = Map.of("newTime", newTime.toString());
        return mockMvc.perform(patch(clockUrl())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private MvcResult createCard(String title) throws Exception {
        BoardJpaEntity board = boardSteps.loadBoardEntity();
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("swimlaneId", board.getSwimlanes().get(0).getId().toString());
        body.put("stageId", board.getStages().get(0).getId().toString());
        return mockMvc.perform(post("/api/boards/" + boardSteps.getCurrentBoardId() + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private Instant currentClockTime() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + boardSteps.getCurrentBoardId())
                        .session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        return Instant.parse((String) readBody(result).get("clockTime"));
    }

    private String currentClockStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + boardSteps.getCurrentBoardId())
                        .session(boardSteps.getSession()))
                .andReturn();
        return (String) readBody(result).get("clockStatus");
    }

    private String clockUrl() {
        return "/api/boards/" + boardSteps.getCurrentBoardId() + "/clock";
    }

    private MockHttpSession loginAsNewUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String username = "board-clock-guest-" + UUID.randomUUID();
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
        return session;
    }

    private Instant parseNarrative(String date, String time) {
        LocalDateTime local = LocalDateTime.parse(date + " " + time, NARRATIVE_FORMAT);
        return local.atZone(ZoneId.systemDefault()).toInstant();
    }

    private void assertCloseTo(Instant expected, Instant actual) {
        Duration diff = Duration.between(expected, actual).abs();
        assertTrue(diff.compareTo(TOLERANCE) <= 0,
                "看板時間應接近 " + expected + "（容許誤差 " + TOLERANCE + "），實際為 " + actual);
    }

    private void syncLastResult() {
        userSteps.setLastResult(lastResult);
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}

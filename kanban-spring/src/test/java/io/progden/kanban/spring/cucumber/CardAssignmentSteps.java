package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.BoardMembershipJpaRepository;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import io.progden.kanban.spring.persistence.UserJpaEntity;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/**
 * spec-user-membership.md「卡片負責人指派」對應的 Cucumber step definitions，透過 MockMvc 打實際
 * 端點，驗證 web／application／persistence 整條路徑。Board／登入身分沿用 {@link BoardSteps}／
 * {@link UserSteps} 已建立的 session（Background 用的是 spec-kanban-basic.md 同一段「我已開啟一個
 * 名為 X 的看板」文字，implementation-loop T-04）。
 */
public class CardAssignmentSteps {

    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private BoardMembershipJpaRepository boardMembershipJpaRepository;

    @Autowired
    private CardJpaRepository cardJpaRepository;

    @Autowired
    private BoardSteps boardSteps;

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private CrossAggregateState crossState;

    private final Map<String, UUID> cardIdsByTitle = new HashMap<>();
    private String currentCardTitle;
    private List<String> pendingAssigneeUsernames;
    private MvcResult lastResult;
    private List<Map<?, ?>> lastCandidateList;
    private List<Map<?, ?>> lastCardsByAssigneeList;
    private long activityCountBefore;

    @Before
    public void resetState() {
        cardIdsByTitle.clear();
        currentCardTitle = null;
        pendingAssigneeUsernames = null;
        lastResult = null;
    }

    // ---- Given ----

    @Given("{string} 與 {string} 都是這個看板的 Member")
    public void givenTwoUsersAreMembersOfThisBoard(String username1, String username2) throws Exception {
        inviteMember(username1);
        inviteMember(username2);
    }

    @Given("看板中存在一張卡片 {string}")
    public void givenCardExistsOnBoard(String title) throws Exception {
        createCard(title);
    }

    @Given("存在另一個使用者 {string}，並非這個看板的成員")
    public void givenNonMemberUserExists(String username) throws Exception {
        ensureUserExists(username);
    }

    @Given("卡片 {string} 目前沒有指派負責人")
    public void givenCardHasNoAssignees(String title) {
        currentCardTitle = title;
        assertTrue(loadCard(title).getAssigneeIds().isEmpty());
    }

    @Given("卡片 {string} 的負責人是 {string} 與 {string}")
    public void givenCardAssignees(String title, String username1, String username2) throws Exception {
        setAssignees(title, List.of(username1, username2));
    }

    @Given("{string} 是卡片 {string} 的負責人")
    public void givenUserIsAssignee(String username, String title) throws Exception {
        setAssignees(title, List.of(username));
    }

    @Given("看板中還有另一張卡片 {string}，負責人也是 {string}")
    public void givenAnotherCardWithAssignee(String title, String username) throws Exception {
        createCard(title);
        setAssignees(title, List.of(username));
    }

    @Given("卡片 {string} 目前的負責人只有 {string}")
    public void givenCardHasSoleAssignee(String title, String username) throws Exception {
        setAssignees(title, List.of(username));
    }

    @Given("卡片 {string} 目前的負責人是 {string} 與 {string}")
    public void givenCardHasTwoAssignees(String title, String username1, String username2) throws Exception {
        setAssignees(title, List.of(username1, username2));
    }

    // ---- When ----

    @When("我開啟卡片 {string} 的詳細編輯畫面")
    public void whenOpenCardDetail(String title) {
        currentCardTitle = title;
    }

    @When("我開啟卡片的詳細編輯畫面")
    public void whenOpenCurrentCardDetail() {
        // currentCardTitle 已由前一個 Given 設定。
    }

    @When("我在「負責人」欄位選擇 {string} 與 {string}")
    public void whenSelectAssignees(String username1, String username2) {
        pendingAssigneeUsernames = List.of(username1, username2);
        crossState.beginSavingAssignees();
    }

    @When("我在負責人欄位中移除 {string}")
    public void whenRemoveAssignee(String username) {
        List<String> current = new ArrayList<>(currentAssigneeUsernames());
        current.remove(username);
        pendingAssigneeUsernames = current;
        crossState.beginSavingAssignees();
    }

    /**
     * 由 {@link CardSteps} 的「我儲存變更」dispatcher 呼叫（見 {@link CrossAggregateState}）。
     */
    void submitPendingAssignees() throws Exception {
        setAssignees(currentCardTitle, pendingAssigneeUsernames);
    }

    @When("我開啟卡片 {string} 的負責人選單")
    public void whenOpenAssigneeCandidates(String title) throws Exception {
        currentCardTitle = title;
        UUID boardId = boardSteps.getCurrentBoardId();
        MvcResult result = mockMvc.perform(get("/api/boards/" + boardId + "/assignee-candidates")
                        .session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        lastCandidateList = readList(result);
    }

    @When("我查看 {string} 負責的卡片清單")
    public void whenViewCardsByAssignee(String username) throws Exception {
        UUID boardId = boardSteps.getCurrentBoardId();
        UUID userId = userIdFor(username);
        MvcResult result = mockMvc.perform(get("/api/boards/" + boardId + "/cards/by-assignee/" + userId)
                        .session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        lastCardsByAssigneeList = readList(result);
    }

    @When("我將 {string} 的頭像拖曳到卡片 {string} 上")
    public void whenDragAvatarToCard(String username, String title) throws Exception {
        UUID cardId = cardIdsByTitle.get(title);
        activityCountBefore = loadCard(title).getActivityLog().size();
        Map<String, String> body = Map.of("userId", userIdFor(username).toString());
        lastResult = mockMvc.perform(post("/api/cards/" + cardId + "/assignees/drag")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, lastResult.getResponse().getStatus(),
                "拖曳追加負責人失敗：" + lastResult.getResponse().getContentAsString());
    }

    // ---- Then ----

    @Then("卡片的負責人應該是 {string} 與 {string}")
    public void thenCardAssigneesAre(String username1, String username2) {
        assertEquals(List.of(userIdFor(username1), userIdFor(username2)), loadCard(currentCardTitle).getAssigneeIds());
    }

    @Then("卡片縮圖應該同時顯示 {string} 與 {string}")
    public void thenCardThumbnailShowsBoth(String username1, String username2) {
        List<UUID> assigneeIds = loadCard(currentCardTitle).getAssigneeIds();
        assertTrue(assigneeIds.contains(userIdFor(username1)));
        assertTrue(assigneeIds.contains(userIdFor(username2)));
    }

    @Then("選單應該顯示 {string} 與 {string}")
    public void thenCandidateListShowsBoth(String username1, String username2) {
        assertTrue(lastCandidateList.stream().anyMatch(c -> username1.equals(c.get("username"))));
        assertTrue(lastCandidateList.stream().anyMatch(c -> username2.equals(c.get("username"))));
    }

    @Then("選單不應該顯示 {string}")
    public void thenCandidateListDoesNotShow(String username) {
        assertFalse(lastCandidateList.stream().anyMatch(c -> username.equals(c.get("username"))));
    }

    @Then("「負責人」欄位應該顯示為未指派")
    public void thenAssigneeFieldUnassigned() {
        assertTrue(loadCard(currentCardTitle).getAssigneeIds().isEmpty());
    }

    @Then("卡片縮圖不應該顯示負責人資訊")
    public void thenCardThumbnailShowsNoAssignee() {
        assertTrue(loadCard(currentCardTitle).getAssigneeIds().isEmpty());
    }

    @Then("卡片的負責人應該只剩下 {string}")
    public void thenCardAssigneeOnlyRemains(String username) {
        assertEquals(List.of(userIdFor(username)), loadCard(currentCardTitle).getAssigneeIds());
    }

    @Then("清單應該同時包含 {string} 與 {string}")
    public void thenListContainsBoth(String title1, String title2) {
        assertTrue(lastCardsByAssigneeList.stream().anyMatch(c -> title1.equals(c.get("title"))));
        assertTrue(lastCardsByAssigneeList.stream().anyMatch(c -> title2.equals(c.get("title"))));
    }

    @Then("卡片 {string} 的負責人應該包含 {string} 與 {string}")
    public void thenCardAssigneesContainBoth(String title, String username1, String username2) {
        List<UUID> assigneeIds = loadCard(title).getAssigneeIds();
        assertTrue(assigneeIds.contains(userIdFor(username1)));
        assertTrue(assigneeIds.contains(userIdFor(username2)));
    }

    @Then("卡片 {string} 的負責人應該仍然只有 {string} 與 {string}")
    public void thenCardAssigneesStillOnlyBoth(String title, String username1, String username2) {
        assertEquals(2, loadCard(title).getAssigneeIds().size());
        List<UUID> assigneeIds = loadCard(title).getAssigneeIds();
        assertTrue(assigneeIds.contains(userIdFor(username1)));
        assertTrue(assigneeIds.contains(userIdFor(username2)));
    }

    @Then("不應該產生新的活動紀錄")
    public void thenNoNewActivityRecorded() {
        assertEquals(activityCountBefore, loadCard(currentCardTitle).getActivityLog().size());
    }

    // ---- helpers ----

    private List<String> currentAssigneeUsernames() {
        List<UUID> ids = loadCard(currentCardTitle).getAssigneeIds();
        return ids.stream()
                .map(id -> userJpaRepository.findById(id).orElseThrow().getUsername())
                .toList();
    }

    private void setAssignees(String title, List<String> usernames) throws Exception {
        currentCardTitle = title;
        for (String username : usernames) {
            inviteMember(username);
        }
        UUID cardId = cardIdsByTitle.get(title);
        List<String> assigneeIds = usernames.stream().map(u -> userIdFor(u).toString()).toList();
        Map<String, Object> body = Map.of("assigneeIds", assigneeIds);
        lastResult = mockMvc.perform(patch("/api/cards/" + cardId + "/assignees")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, lastResult.getResponse().getStatus(),
                "設定負責人失敗：" + lastResult.getResponse().getContentAsString());
    }

    private void inviteMember(String username) throws Exception {
        ensureUserExists(username);
        UUID boardId = boardSteps.getCurrentBoardId();
        UUID userId = userIdFor(username);
        if (boardMembershipJpaRepository.findByBoardIdAndUserId(boardId, userId).isPresent()) {
            return;
        }
        Map<String, String> body = Map.of("username", username, "role", "MEMBER");
        mockMvc.perform(post("/api/boards/" + boardId + "/members")
                .session(boardSteps.getSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void ensureUserExists(String username) throws Exception {
        if (userJpaRepository.existsByUsername(username)) {
            return;
        }
        Map<String, String> body = Map.of("username", username, "password", DEFAULT_PASSWORD);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void createCard(String title) throws Exception {
        UUID boardId = boardSteps.getCurrentBoardId();
        BoardJpaEntity board = boardJpaRepository.findById(boardId).orElseThrow();
        Map<String, String> body = Map.of("title", title,
                "swimlaneId", board.getSwimlanes().get(0).getId().toString(),
                "stageId", board.getStages().get(0).getId().toString());
        MvcResult result = mockMvc.perform(post("/api/boards/" + boardId + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
        cardIdsByTitle.put(title, UUID.fromString((String) readBody(result).get("id")));
        currentCardTitle = title;
    }

    private CardJpaEntity loadCard(String title) {
        UUID cardId = cardIdsByTitle.get(title);
        return cardJpaRepository.findById(cardId).orElseThrow();
    }

    private UUID userIdFor(String username) {
        UserJpaEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("找不到帳號：" + username));
        return user.getId();
    }

    private List<Map<?, ?>> readList(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}

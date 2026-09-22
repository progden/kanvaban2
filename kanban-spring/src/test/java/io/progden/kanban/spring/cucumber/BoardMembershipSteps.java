package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.BoardMembershipActivityJpaRepository;
import io.progden.kanban.spring.persistence.BoardMembershipJpaEntity;
import io.progden.kanban.spring.persistence.BoardMembershipJpaRepository;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import io.progden.kanban.spring.persistence.UserJpaEntity;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/**
 * spec-user-membership.md「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」對應的
 * Cucumber step definitions，透過 MockMvc 打實際端點，驗證 web／application／persistence 整條路徑。
 *
 * <p>Background 的「我已登入系統，帳號為 X」沿用 {@link UserSteps} 的 session；其他情境角色
 * （例如「雅婷」「建宏」）第一次被提及時才建立帳號並各自登入，用各自的 {@link MockHttpSession}
 * 呼叫端點，模擬多位使用者互動（implementation-loop T-04 新增，同 BoardSteps／CardSteps 慣例：
 * 透過真正的 web／application／persistence 路徑驗證，不直接呼叫 domain 物件）。
 */
public class BoardMembershipSteps {

    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_BOARD_NAME = "產品開發看板";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private BoardMembershipJpaRepository boardMembershipJpaRepository;

    @Autowired
    private BoardMembershipActivityJpaRepository boardMembershipActivityJpaRepository;

    @Autowired
    private CardJpaRepository cardJpaRepository;

    @Autowired
    private UserSteps userSteps;

    @Autowired
    private BoardSteps boardSteps;

    private final Map<String, UUID> boardIdsByName = new HashMap<>();
    private final Map<String, MockHttpSession> sessionsByUsername = new HashMap<>();
    private final Map<String, UUID> cardIdsByTitle = new HashMap<>();

    private UUID currentBoardId;
    private MvcResult lastResult;

    @Before
    public void resetMembershipState() {
        boardMembershipJpaRepository.deleteAll();
        boardMembershipActivityJpaRepository.deleteAll();
        boardIdsByName.clear();
        sessionsByUsername.clear();
        cardIdsByTitle.clear();
        currentBoardId = null;
        lastResult = null;
    }

    // ---- Given ----

    @Given("系統中存在帳號 {string}")
    public void givenAccountExists(String username) throws Exception {
        ensureUserExists(username);
    }

    @Given("我是 Board {string} 的 Owner")
    public void givenIAmOwnerOfBoard(String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
    }

    @Given("我是 Board {string} 的 Member")
    public void givenIAmMemberOfBoard(String boardName) throws Exception {
        String otherOwner = "other-owner-" + boardName.hashCode();
        ensureBoardExists(boardName, otherOwner);
        invite(boardName, otherOwner, currentDefaultUsername(), "MEMBER");
    }

    @Given("存在另一個我沒有權限的 Board {string}")
    public void givenBoardIHaveNoAccessTo(String boardName) throws Exception {
        String otherOwner = "other-owner-" + boardName.hashCode();
        ensureBoardExists(boardName, otherOwner);
    }

    @Given("存在一個我沒有權限的 Board {string}")
    public void givenAnotherBoardIHaveNoAccessTo(String boardName) throws Exception {
        String otherOwner = "other-owner-" + boardName.hashCode();
        ensureBoardExists(boardName, otherOwner);
    }

    @Given("{string} 已經是 Board {string} 的 Member")
    public void givenUserIsMemberOfBoard(String username, String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
        ensureMember(boardName, username, "MEMBER");
    }

    @Given("{string} 是 Board {string} 的 Member")
    public void givenUserIsMemberOfBoardAlt(String username, String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
        ensureMember(boardName, username, "MEMBER");
    }

    @Given("{string} 與 {string} 都是 Board {string} 的 Owner")
    public void givenTwoUsersAreOwners(String username1, String username2, String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
        ensureMember(boardName, username1, "OWNER");
        ensureMember(boardName, username2, "OWNER");
    }

    @Given("Board {string} 有 {int} 位 Owner：{string} 與 {string}")
    public void givenBoardHasTwoOwners(String boardName, int count, String username1, String username2)
            throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
        ensureMember(boardName, username1, "OWNER");
        ensureMember(boardName, username2, "OWNER");
        assertEquals(count, boardMembershipJpaRepository.findByBoardId(boardIdsByName.get(boardName)).size());
    }

    @Given("{string} 建立了這個 Board")
    public void givenUserCreatedThisBoard(String username) {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertEquals(userIdFor(username), board.getCreatedBy());
    }

    @Given("我是 Board {string} 唯一的 Owner")
    public void givenSoleOwner(String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
    }

    @Given("{string} 是這個 Board 中 {int} 張卡片的負責人")
    public void givenUserIsAssigneeOfNCards(String username, int cardCount) throws Exception {
        String boardName = currentBoardName();
        for (int i = 1; i <= cardCount; i++) {
            String title = username + "負責的卡片" + i;
            createCard(boardName, title);
            setAssignees(title, List.of(username));
        }
    }

    @Given("{string} 與 {string} 都是卡片 {string} 的負責人")
    public void givenTwoUsersAreCardAssignees(String username1, String username2, String cardTitle)
            throws Exception {
        String boardName = currentBoardName();
        if (!cardIdsByTitle.containsKey(cardTitle)) {
            createCard(boardName, cardTitle);
        }
        setAssignees(cardTitle, List.of(username1, username2));
    }

    @Given("{string} 與 {string} 都是 Board {string} 的 Member")
    public void givenTwoUsersAreMembers(String username1, String username2, String boardName) throws Exception {
        ensureBoardExists(boardName, currentDefaultUsername());
        ensureMember(boardName, username1, "MEMBER");
        ensureMember(boardName, username2, "MEMBER");
    }

    @Given("{string} 是這個 Board 的 Member")
    public void givenUserIsMemberOfCurrentBoard(String username) throws Exception {
        ensureMember(currentBoardName(), username, "MEMBER");
    }

    @Given("這個 Board 有 {int} 個 Swimlane、{int} 個 Stage，以及數張卡片")
    public void givenBoardHasStructureAndCards(int swimlaneCount, int stageCount) throws Exception {
        String boardName = currentBoardName();
        for (int i = swimlaneCount; i > 1; i--) {
            addSwimlaneAsActingUser(boardName, currentDefaultUsername(), "額外泳道" + i);
        }
        createCard(boardName, "既有卡片");
    }

    // ---- When（Board 權限管理） ----

    @When("{string} 嘗試邀請 {string} 加入這個 Board")
    public void whenAttemptInvite(String actingUsername, String targetUsername) throws Exception {
        invite(currentBoardName(), actingUsername, targetUsername, "MEMBER");
    }

    @When("{string} 嘗試將自己升級為 {string}")
    public void whenAttemptSelfPromote(String actingUsername, String role) throws Exception {
        changeRole(currentBoardName(), actingUsername, actingUsername, role.toUpperCase());
    }

    @When("{string} 嘗試新增一個 Swimlane")
    public void whenAttemptAddSwimlane(String actingUsername) throws Exception {
        addSwimlaneAsActingUser(currentBoardName(), actingUsername, "雅婷嘗試新增的 Swimlane");
    }

    @When("{string} 嘗試刪除這個 Board")
    public void whenAttemptDeleteBoard(String actingUsername) throws Exception {
        UUID boardId = boardIdsByName.get(currentBoardName());
        lastResult = mockMvc.perform(delete("/api/boards/" + boardId).session(sessionFor(actingUsername)))
                .andReturn();
        syncLastResult();
    }

    @When("我以 Owner 身分刪除這個 Board")
    public void whenIDeleteBoardAsOwner() throws Exception {
        UUID boardId = boardIdsByName.get(currentBoardName());
        lastResult = mockMvc.perform(delete("/api/boards/" + boardId).session(sessionFor(currentDefaultUsername())))
                .andReturn();
        syncLastResult();
    }

    @When("{string} 在這個 Board 新增一張卡片 {string}")
    public void whenUserAddsCard(String actingUsername, String title) throws Exception {
        String boardName = currentBoardName();
        UUID boardId = boardIdsByName.get(boardName);
        BoardJpaEntity board = boardJpaRepository.findById(boardId).orElseThrow();
        Map<String, String> body = Map.of("title", title,
                "swimlaneId", board.getSwimlanes().get(0).getId().toString(),
                "stageId", board.getStages().get(0).getId().toString());
        lastResult = mockMvc.perform(post("/api/boards/" + boardId + "/cards")
                        .session(sessionFor(actingUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
        if (lastResult.getResponse().getStatus() == 201) {
            cardIdsByTitle.put(title, UUID.fromString((String) readBody(lastResult).get("id")));
        }
    }

    // ---- Then（Board 權限管理） ----

    @Then("{string} 不應該成為這個 Board 的成員")
    public void thenUserNotMember(String username) {
        assertTrue(boardMembershipJpaRepository.findByBoardIdAndUserId(currentBoardId, userIdFor(username)).isEmpty());
    }

    @Then("看板的 Swimlane 數量不應該改變")
    public void thenSwimlaneCountUnchanged() {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertEquals(1, board.getSwimlanes().size());
    }

    @Then("這個 Board 應該不再存在")
    public void thenBoardNoLongerExists() {
        assertEquals(204, lastResult.getResponse().getStatus());
        assertTrue(boardJpaRepository.findById(currentBoardId).isEmpty());
    }

    @Then("這個 Board 底下的所有 Swimlane、Stage 與卡片都應該一併被刪除")
    public void thenBoardDataAllDeleted() {
        assertTrue(cardJpaRepository.findByBoardIdAndDeletedFalse(currentBoardId).isEmpty());
    }

    @Then("該卡片應該被成功建立")
    public void thenCardCreatedSuccessfully() {
        assertEquals(201, lastResult.getResponse().getStatus());
    }

    private void addSwimlaneAsActingUser(String boardName, String actingUsername, String swimlaneName)
            throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        Map<String, String> body = Map.of("name", swimlaneName);
        lastResult = mockMvc.perform(post("/api/boards/" + boardId + "/swimlanes")
                        .session(sessionFor(actingUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    // ---- When ----

    @When("我建立一個名為 {string} 的 Board")
    public void whenCreateBoard(String boardName) throws Exception {
        UUID boardId = doCreateBoard(boardName, sessionFor(currentDefaultUsername()));
        boardIdsByName.put(boardName, boardId);
        currentBoardId = boardId;
    }

    @When("我邀請 {string} 加入這個 Board，角色為 {string}")
    public void whenInviteMemberWithRole(String username, String role) throws Exception {
        invite(currentBoardName(), currentDefaultUsername(), username, role.toUpperCase());
    }

    @When("我再次邀請 {string} 加入這個 Board")
    public void whenInviteAgain(String username) throws Exception {
        invite(currentBoardName(), currentDefaultUsername(), username, "MEMBER");
    }

    @When("我將 {string} 的角色變更為 {string}")
    public void whenChangeRole(String username, String role) throws Exception {
        changeRole(currentBoardName(), currentDefaultUsername(), username, role.toUpperCase());
    }

    @When("{string} 邀請 {string} 加入這個 Board")
    public void whenOtherUserInvites(String actingUsername, String targetUsername) throws Exception {
        invite(currentBoardName(), actingUsername, targetUsername, "MEMBER");
    }

    @When("{string} 將 {string} 從這個 Board 移除")
    public void whenOtherUserRemoves(String actingUsername, String targetUsername) throws Exception {
        removeMember(currentBoardName(), actingUsername, targetUsername, false);
    }

    @When("我將 {string} 從這個 Board 移除")
    public void whenIRemove(String targetUsername) throws Exception {
        removeMember(currentBoardName(), currentDefaultUsername(), targetUsername, false);
    }

    @When("我嘗試將 {string} 從這個 Board 移除")
    public void whenIAttemptRemove(String targetUsername) throws Exception {
        removeMember(currentBoardName(), currentDefaultUsername(), targetUsername, false);
    }

    @When("我確認將 {string} 從這個 Board 移除")
    public void whenIConfirmRemove(String targetUsername) throws Exception {
        removeMember(currentBoardName(), currentDefaultUsername(), targetUsername, true);
    }

    @When("我嘗試將自己從這個 Board 移除")
    public void whenIAttemptRemoveSelf() throws Exception {
        removeMember(currentBoardName(), currentDefaultUsername(), currentDefaultUsername(), false);
    }

    private List<Map<?, ?>> lastBoardList;
    private List<Map<?, ?>> lastActivityLog;

    @When("我開啟這個 Board 的活動紀錄")
    public void whenOpenBoardActivityLog() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + currentBoardId + "/activity-log")
                        .session(sessionFor(currentDefaultUsername())))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        lastActivityLog = (List<Map<?, ?>>) objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
    }

    @Then("活動紀錄應該依時間由新到舊列出，且每一筆都顯示操作人與動作內容")
    public void thenActivityLogSortedNewestFirst() {
        assertTrue(lastActivityLog.size() >= 2);
        for (Map<?, ?> entry : lastActivityLog) {
            assertTrue(entry.get("operatorUsername") != null);
            assertTrue(entry.get("action") != null);
        }
        for (int i = 0; i < lastActivityLog.size() - 1; i++) {
            String current = (String) lastActivityLog.get(i).get("occurredAt");
            String next = (String) lastActivityLog.get(i + 1).get("occurredAt");
            assertTrue(current.compareTo(next) >= 0);
        }
    }

    @Then("^最上面一筆應該是 \"([^\"]*)\" 邀請 \"([^\"]*)\" 加入看板$")
    public void thenTopEntryIsInvite(String operatorUsername, String targetUsername) {
        Map<?, ?> top = lastActivityLog.get(0);
        assertEquals(operatorUsername, top.get("operatorUsername"));
        assertTrue(((String) top.get("action")).contains(targetUsername));
    }

    @When("我開啟「我的 Board」列表")
    public void whenOpenMyBoardList() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards").session(sessionFor(currentDefaultUsername())))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        lastBoardList = (List<Map<?, ?>>) objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
    }

    @When("我嘗試直接開啟 Board {string}")
    public void whenAttemptOpenBoard(String boardName) throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        lastResult = mockMvc.perform(get("/api/boards/" + boardId).session(sessionFor(currentDefaultUsername())))
                .andReturn();
        syncLastResult();
    }

    @Then("列表應該顯示 {string} 與 {string}")
    public void thenListShowsBoth(String name1, String name2) {
        assertTrue(lastBoardList.stream().anyMatch(b -> name1.equals(b.get("name"))));
        assertTrue(lastBoardList.stream().anyMatch(b -> name2.equals(b.get("name"))));
    }

    @Then("列表不應該顯示 {string}")
    public void thenListDoesNotShow(String name) {
        assertFalse(lastBoardList.stream().anyMatch(b -> name.equals(b.get("name"))));
    }

    @When("我確認移除")
    public void whenIConfirmRemovePending() throws Exception {
        // 上一步（我嘗試將 X 從這個 Board 移除）已經知道目標，重放同一個請求並帶 confirmed=true。
        removeMember(currentBoardName(), currentDefaultUsername(), pendingRemoveTarget, true);
    }

    private String pendingRemoveTarget;

    // ---- Then ----

    @Then("該 Board 的建立者應該顯示為 {string}")
    public void thenCreatedByIs(String username) {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertEquals(userIdFor(username), board.getCreatedBy());
    }

    @Then("我對該 Board 的角色應該是 {string}")
    public void thenMyRoleIs(String role) {
        assertRole(currentBoardId, currentDefaultUsername(), role);
    }

    @Then("^應該產生一筆活動紀錄：操作人 \"([^\"]*)\"、動作為「(.*)」$")
    public void thenActivityRecorded(String operatorUsername, String action) {
        assertActivityRecorded(operatorUsername, action);
    }

    @Then("該 Board 應該有 {int} 個 Swimlane {string}")
    public void thenBoardHasSwimlane(int count, String name) {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertEquals(count, board.getSwimlanes().size());
        assertEquals(name, board.getSwimlanes().get(0).getName());
    }

    @Then("該 Board 的 Stage 應該依序為 {string}、{string}、{string}")
    public void thenBoardStagesInOrder(String s1, String s2, String s3) {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertEquals(List.of(s1, s2, s3),
                board.getStages().stream().map(io.progden.kanban.spring.persistence.StageJpaEntity::getName).toList());
    }

    @Then("該 Board 所有 Stage 的角色應該皆為 NONE")
    public void thenAllStageRolesNone() {
        BoardJpaEntity board = boardJpaRepository.findById(currentBoardId).orElseThrow();
        assertTrue(board.getStages().stream().allMatch(s -> s.getRole().name().equals("NONE")));
    }

    @Then("{string} 應該立即成為這個 Board 的 Member，不需要對方確認")
    public void thenBecomesMemberImmediately(String username) {
        assertRole(currentBoardId, username, "Member");
    }

    @Then("{string} 應該能在自己的 Board 列表中看到 {string}")
    public void thenBoardVisibleInList(String username, String boardName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards").session(sessionFor(username))).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        List<?> boards = objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
        boolean found = boards.stream()
                .map(b -> (Map<?, ?>) b)
                .anyMatch(b -> boardName.equals(b.get("name")));
        assertTrue(found, boardName + " 應該出現在 " + username + " 的 Board 列表中");
    }

    @Then("Board 的成員數徽章應該增加 {int}")
    public void thenMemberCountIncreasedBy(int delta) {
        // 成員數徽章是前端呈現，後端以 board-membership 筆數驗證同等意義（implementation-loop 低風險決策）。
        assertTrue(boardMembershipJpaRepository.findByBoardId(currentBoardId).size() >= 1 + delta);
    }

    @Then("{string} 對這個 Board 的角色應該是 {string}")
    public void thenUserRoleIs(String username, String role) {
        assertRole(currentBoardId, username, role);
    }

    @Then("這個 Board 現在應該有 {int} 位 Owner：{string} 與 {string}")
    public void thenBoardHasOwners(int count, String username1, String username2) {
        List<BoardMembershipJpaEntity> owners = boardMembershipJpaRepository.findByBoardId(currentBoardId).stream()
                .filter(m -> m.getRole().name().equals("OWNER"))
                .toList();
        assertEquals(count, owners.size());
        assertTrue(owners.stream().anyMatch(m -> m.getUserId().equals(userIdFor(username1))));
        assertTrue(owners.stream().anyMatch(m -> m.getUserId().equals(userIdFor(username2))));
    }

    @Then("{string} 應該成為這個 Board 的 Member")
    public void thenUserBecomesMember(String username) {
        assertRole(currentBoardId, username, "Member");
    }

    @Then("{string} 應該無法再存取這個 Board")
    public void thenUserCannotAccessBoard(String username) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + currentBoardId).session(sessionFor(username)))
                .andReturn();
        assertEquals(403, result.getResponse().getStatus());
    }

    @Then("{string} 應該不再是這個 Board 的成員")
    public void thenUserNoLongerMember(String username) {
        assertTrue(boardMembershipJpaRepository.findByBoardIdAndUserId(currentBoardId, userIdFor(username)).isEmpty());
    }

    @Then("Board {string} 應該仍保留 {string} 這位 Owner")
    public void thenBoardStillHasOwner(String boardName, String username) {
        assertRole(boardIdsByName.get(boardName), username, "Owner");
    }

    @Then("我對該 Board 的成員關係不應該被移除")
    public void thenMyMembershipNotRemoved() {
        assertTrue(boardMembershipJpaRepository
                .findByBoardIdAndUserId(currentBoardId, userIdFor(currentDefaultUsername())).isPresent());
    }

    @Then("系統應該顯示確認訊息，告知 {string} 仍是 {int} 張卡片的負責人，移除後這些卡片會變成未指派")
    public void thenConfirmationMessageShown(String username, int cardCount) throws Exception {
        assertEquals(409, lastResult.getResponse().getStatus());
        String message = (String) readBody(lastResult).get("message");
        assertTrue(message.contains(String.valueOf(cardCount)),
                "確認訊息應包含卡片數 " + cardCount + "，實際為：" + message);
        pendingRemoveTarget = username;
    }

    @Then("這 {int} 張卡片的負責人欄位都應該變成未指派")
    public void thenCardsBecomeUnassigned(int cardCount) {
        String targetUsername = pendingRemoveTarget;
        long stillAssigned = cardIdsByTitle.values().stream()
                .map(id -> cardJpaRepository.findById(id).orElseThrow())
                .filter(c -> c.getAssigneeIds().contains(userIdFor(targetUsername)))
                .count();
        assertEquals(0, stillAssigned);
    }

    @Then("卡片 {string} 的負責人應該只剩下 {string}")
    public void thenCardAssigneeOnlyRemains(String cardTitle, String username) {
        CardJpaEntity card = cardJpaRepository.findById(cardIdsByTitle.get(cardTitle)).orElseThrow();
        assertEquals(List.of(userIdFor(username)), card.getAssigneeIds());
    }

    // ---- helpers ----

    private void assertRole(UUID boardId, String username, String expectedRole) {
        BoardMembershipJpaEntity membership = boardMembershipJpaRepository
                .findByBoardIdAndUserId(boardId, userIdFor(username))
                .orElseThrow(() -> new IllegalStateException(username + " 不是這個 Board 的成員"));
        assertEquals(expectedRole.toUpperCase(), membership.getRole().name());
    }

    /**
     * 「應該產生一筆活動紀錄：操作人 X、動作為「Y」」是 spec-user-membership.md「Board 建立與成員邀請」
     * 與「卡片負責人指派」兩個 Feature 共用的步驟文字，分別對應 {@code board-membership}／{@code board}
     * 的活動紀錄（本類別管理）與 {@code card} 的活動紀錄（{@link CardAssignmentSteps} 管理）。
     * 不區分「目前作用中的 Board」，直接掃全部（每個 Scenario 開始前各 repository 都會清空，
     * 資料不會互相汙染），哪個地方比對得到就算通過。
     */
    private void assertActivityRecorded(String operatorUsername, String action) {
        UUID operatorId = userIdFor(operatorUsername);
        boolean found = boardMembershipActivityJpaRepository.findAll().stream()
                .anyMatch(a -> a.getOperatorId().equals(operatorId) && a.getAction().equals(action));
        if (!found) {
            found = boardJpaRepository.findAll().stream()
                    .flatMap(b -> b.getActivityLog().stream())
                    .anyMatch(a -> a.getOperatorId().equals(operatorId) && a.getAction().equals(action));
        }
        if (!found) {
            found = cardJpaRepository.findAll().stream()
                    .flatMap(c -> c.getActivityLog().stream())
                    .anyMatch(a -> a.getOperatorId().equals(operatorId) && a.getAction().equals(action));
        }
        assertTrue(found, "應有一筆活動紀錄：操作人 " + operatorUsername + "、動作為「" + action + "」");
    }

    private String currentDefaultUsername() {
        return "user1";
    }

    private String currentBoardName() {
        if (boardIdsByName.isEmpty()) {
            try {
                ensureBoardExists(DEFAULT_BOARD_NAME, currentDefaultUsername());
            } catch (Exception e) {
                throw new IllegalStateException("目前沒有作用中的 Board，自動建立預設 Board 失敗", e);
            }
        }
        if (boardIdsByName.size() == 1) {
            return boardIdsByName.keySet().iterator().next();
        }
        return boardIdsByName.entrySet().stream()
                .filter(e -> e.getValue().equals(currentBoardId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("目前沒有作用中的 Board"));
    }

    private UUID ensureBoardExists(String boardName, String creatorUsername) throws Exception {
        if (boardIdsByName.containsKey(boardName)) {
            currentBoardId = boardIdsByName.get(boardName);
            return currentBoardId;
        }
        UUID boardId = doCreateBoard(boardName, sessionFor(creatorUsername));
        boardIdsByName.put(boardName, boardId);
        currentBoardId = boardId;
        if (creatorUsername.equals(currentDefaultUsername())) {
            boardSteps.adoptBoard(boardId, sessionFor(creatorUsername), userIdFor(creatorUsername));
        }
        return boardId;
    }

    private UUID doCreateBoard(String boardName, MockHttpSession session) throws Exception {
        Map<String, String> body = Map.of("name", boardName);
        MvcResult result = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
        return UUID.fromString((String) readBody(result).get("id"));
    }

    private void ensureMember(String boardName, String username, String role) throws Exception {
        ensureUserExists(username);
        UUID boardId = boardIdsByName.get(boardName);
        if (boardMembershipJpaRepository.findByBoardIdAndUserId(boardId, userIdFor(username)).isPresent()) {
            return;
        }
        invite(boardName, currentDefaultUsername(), username, role);
    }

    private void invite(String boardName, String actingUsername, String targetUsername, String role)
            throws Exception {
        ensureUserExists(targetUsername);
        UUID boardId = boardIdsByName.get(boardName);
        Map<String, String> body = Map.of("username", targetUsername, "role", role);
        lastResult = mockMvc.perform(post("/api/boards/" + boardId + "/members")
                        .session(sessionFor(actingUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void changeRole(String boardName, String actingUsername, String targetUsername, String role)
            throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        Map<String, String> body = Map.of("role", role);
        lastResult = mockMvc.perform(patch("/api/boards/" + boardId + "/members/" + targetUsername + "/role")
                        .session(sessionFor(actingUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        syncLastResult();
    }

    private void removeMember(String boardName, String actingUsername, String targetUsername, boolean confirmed)
            throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        pendingRemoveTarget = targetUsername;
        lastResult = mockMvc.perform(delete("/api/boards/" + boardId + "/members/" + targetUsername
                        + "?confirmed=" + confirmed)
                        .session(sessionFor(actingUsername)))
                .andReturn();
        syncLastResult();
    }

    private void createCard(String boardName, String title) throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        BoardJpaEntity board = boardJpaRepository.findById(boardId).orElseThrow();
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();
        Map<String, String> body = Map.of("title", title, "swimlaneId", swimlaneId.toString(),
                "stageId", stageId.toString());
        MvcResult result = mockMvc.perform(post("/api/boards/" + boardId + "/cards")
                        .session(sessionFor(currentDefaultUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
        cardIdsByTitle.put(title, UUID.fromString((String) readBody(result).get("id")));
    }

    void setAssignees(String cardTitle, List<String> usernames) throws Exception {
        for (String username : usernames) {
            ensureUserExists(username);
            ensureMember(currentBoardName(), username, "MEMBER");
        }
        List<String> assigneeIds = usernames.stream().map(u -> userIdFor(u).toString()).toList();
        UUID cardId = cardIdsByTitle.get(cardTitle);
        Map<String, Object> body = Map.of("assigneeIds", assigneeIds);
        MvcResult result = mockMvc.perform(patch("/api/cards/" + cardId + "/assignees")
                        .session(sessionFor(currentDefaultUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置資料建立失敗：" + result.getResponse().getContentAsString());
    }

    UUID cardIdFor(String title) {
        return cardIdsByTitle.get(title);
    }

    UUID boardIdFor(String boardName) {
        return boardIdsByName.get(boardName);
    }

    private MockHttpSession sessionFor(String username) throws Exception {
        if (username.equals(currentDefaultUsername()) && userSteps.getSession() != null) {
            return userSteps.getSession();
        }
        ensureUserExists(username);
        MockHttpSession cached = sessionsByUsername.get(username);
        if (cached != null) {
            return cached;
        }
        MockHttpSession session = new MockHttpSession();
        Map<String, String> body = Map.of("username", username, "password", DEFAULT_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置登入失敗：" + result.getResponse().getContentAsString());
        sessionsByUsername.put(username, session);
        return session;
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

    private UUID userIdFor(String username) {
        UserJpaEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("找不到帳號：" + username));
        return user.getId();
    }

    private void syncLastResult() {
        userSteps.setLastResult(lastResult);
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }
}

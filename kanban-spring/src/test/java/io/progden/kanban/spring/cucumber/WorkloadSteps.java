package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardMembershipJpaRepository;
import io.progden.kanban.spring.persistence.StageJpaEntity;
import io.progden.kanban.spring.persistence.UserJpaEntity;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/**
 * spec-workload.md「Feature: 人員工作量檢視」對應的 Cucumber step definitions，透過 MockMvc 打
 * {@code WorkloadController} 的實際端點，驗證 web／query／persistence 整條路徑。
 *
 * <p>登入與開板沿用 {@link BoardSteps} 已註冊的共用步驟（「我已登入系統，並開啟 Board X」），這裡不
 * 重複定義；「Stage X 已設定角色為 Y」在本檔另外定義（{@link #givenSingleStageRole}，轉呼叫
 * {@link BoardSteps#whenSetStageRole}）。「拖曳成員頭像追加負責人」的兩個 Scenario 與
 * spec-user-membership.md {@code uc-assign-card-owner-by-drag} 是同一個操作，步驟文字與
 * {@link CardAssignmentSteps} 完全相同，共用其定義（見 spec-workload.md「Use Case 定義」下方說明）。
 */
public class WorkloadSteps {

    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BoardMembershipJpaRepository boardMembershipJpaRepository;

    @Autowired
    private BoardSteps boardSteps;

    private final Map<String, UUID> cardIdsByTitle = new HashMap<>();
    private final Map<String, String> displayNamesByUsername = new HashMap<>();
    private Map<?, ?> lastBody;

    @Before
    public void resetWorkloadState() {
        cardIdsByTitle.clear();
        displayNamesByUsername.clear();
        lastBody = null;
    }

    // ---- Given ----

    @Given("看板成員包含 {string} 與 {string}")
    public void givenBoardMembersInclude(String username1, String username2) throws Exception {
        inviteMember(username1);
        inviteMember(username2);
    }

    @Given("Stage {string} 已設定角色為 {word}")
    public void givenSingleStageRole(String stageName, String role) throws Exception {
        boardSteps.whenSetStageRole(stageName, role.toUpperCase());
    }

    @Given("{string} 是 {int} 張進行中卡片的負責人")
    public void givenUserIsAssigneeOfActiveCards(String username, int count) throws Exception {
        inviteMember(username);
        for (int i = 1; i <= count; i++) {
            String title = username + "-進行中卡片-" + i;
            UUID cardId = createCard(title, todoStageId());
            setAssignees(cardId, List.of(username));
        }
    }

    @Given("卡片 {string} 的負責人同時是 {string} 與 {string}，且尚未完成")
    public void givenCardHasTwoAssigneesNotDone(String title, String username1, String username2) throws Exception {
        inviteMember(username1);
        inviteMember(username2);
        UUID cardId = createCard(title, todoStageId());
        setAssignees(cardId, List.of(username1, username2));
    }

    @Given("有 {int} 張進行中卡片沒有指派任何負責人")
    public void givenUnassignedActiveCards(int count) throws Exception {
        for (int i = 1; i <= count; i++) {
            createCard("未指派卡片-" + i, todoStageId());
        }
    }

    @Given("{string} 是 {int} 張已進入 Done 角色 Stage 的卡片的負責人")
    public void givenUserIsAssigneeOfDoneCards(String username, int count) throws Exception {
        inviteMember(username);
        for (int i = 1; i <= count; i++) {
            String title = username + "-已完成卡片-" + i;
            UUID cardId = createCard(title, doneStageId());
            setAssignees(cardId, List.of(username));
        }
    }

    // ---- When ----

    @When("我開啟 Workload 表")
    public void whenOpenWorkloadTable() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + boardSteps.getCurrentBoardId() + "/workload")
                        .session(boardSteps.getSession()))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "開啟 Workload 表失敗：" + result.getResponse().getContentAsString());
        lastBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    // ---- Then ----

    @Then("{string} 的工作量應該顯示為 {int}")
    public void thenWorkloadIs(String username, int expected) {
        assertEquals(expected, workloadOf(username));
        assertEquals(displayNameFor(username), memberFieldOf(username, "displayName"),
                "回應的 displayName 應該是 user.display-name，不是 username");
    }

    @Then("{string} 的工作量應該包含卡片 {string}")
    public void thenWorkloadIncludesCard(String username, String cardTitle) {
        assertEquals(1, workloadOf(username), "「" + username + "」的工作量應該包含卡片 " + cardTitle);
    }

    @Then("{string} 的卡片數量應該顯示為 {int}")
    public void thenUnassignedCountIs(String label, int expected) {
        assertEquals("未指派", label, "此步驟目前只支援「未指派」標籤");
        assertEquals(expected, ((Number) lastBody.get("unassignedCount")).intValue());
    }

    @Then("{string} 的工作量不應該包含該卡片")
    public void thenWorkloadExcludesCard(String username) {
        assertEquals(0, workloadOf(username));
    }

    // ---- helpers ----

    private int workloadOf(String username) {
        return ((Number) memberFieldOf(username, "cardCount")).intValue();
    }

    private Object memberFieldOf(String username, String field) {
        UUID userId = userIdFor(username);
        List<?> members = (List<?>) lastBody.get("members");
        return members.stream()
                .map(m -> (Map<?, ?>) m)
                .filter(m -> userId.toString().equals(m.get("userId")))
                .findFirst()
                .<Object>map(m -> m.get(field))
                .orElse(field.equals("cardCount") ? Integer.valueOf(0) : null);
    }

    private String displayNameFor(String username) {
        return displayNamesByUsername.get(username);
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
        String displayName = username + "－顯示名稱";
        displayNamesByUsername.put(username, displayName);
        Map<String, String> body = Map.of("username", username, "displayName", displayName, "password", DEFAULT_PASSWORD);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private UUID createCard(String title, UUID stageId) throws Exception {
        UUID boardId = boardSteps.getCurrentBoardId();
        UUID swimlaneId = boardSteps.loadBoardEntity().getSwimlanes().get(0).getId();
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("swimlaneId", swimlaneId.toString());
        body.put("stageId", stageId.toString());
        MvcResult result = mockMvc.perform(post("/api/boards/" + boardId + "/cards")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus(),
                "測試前置資料建立卡片失敗：" + result.getResponse().getContentAsString());
        Map<?, ?> responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        UUID cardId = UUID.fromString((String) responseBody.get("id"));
        cardIdsByTitle.put(title, cardId);
        return cardId;
    }

    private void setAssignees(UUID cardId, List<String> usernames) throws Exception {
        List<String> assigneeIds = usernames.stream().map(u -> userIdFor(u).toString()).toList();
        Map<String, Object> body = Map.of("assigneeIds", assigneeIds);
        MvcResult result = mockMvc.perform(patch("/api/cards/" + cardId + "/assignees")
                        .session(boardSteps.getSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus(),
                "測試前置資料設定負責人失敗：" + result.getResponse().getContentAsString());
    }

    private UUID userIdFor(String username) {
        UserJpaEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("找不到帳號：" + username));
        return user.getId();
    }

    private UUID todoStageId() {
        return resolveStageId("待辦");
    }

    private UUID doneStageId() {
        return boardSteps.loadBoardEntity().getStages().stream()
                .filter(s -> s.getRole().name().equals("DONE"))
                .map(StageJpaEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到角色為 DONE 的 Stage"));
    }

    private UUID resolveStageId(String name) {
        BoardJpaEntity board = boardSteps.loadBoardEntity();
        return board.getStages().stream()
                .filter(s -> s.getName().equals(name))
                .map(StageJpaEntity::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到 Stage " + name));
    }
}

package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * spec-user-membership.md「建立使用者帳號」「使用者登入與登出」對應的 Cucumber step definitions，
 * 透過 MockMvc 打 {@code UserController} 的實際端點，驗證 web／application／persistence 整條路徑。
 */
public class UserSteps {

    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private MockHttpSession session;
    private MvcResult lastResult;

    /**
     * 供其他 aggregate 的 step definitions（例如 {@code BoardSteps}）重用「系統應該顯示錯誤訊息」這類
     * 共用斷言步驟：Cucumber 同一段文字只能對應一個 step definition，不能各自重複宣告，
     * 呼叫端在自己執行完 MockMvc 動作後呼叫這個方法同步結果，純新增、不影響既有行為（T-02 補上）。
     */
    void setLastResult(MvcResult result) {
        this.lastResult = result;
    }
    private long countBeforeAction;
    private String pendingLoginUsername;
    private String pendingLoginPassword;

    @Before
    public void resetState() {
        userJpaRepository.deleteAll();
        session = new MockHttpSession();
        lastResult = null;
    }

    // ---- Given ----

    @Given("系統中已存在帳號 {string}")
    public void givenAccountExists(String username) throws Exception {
        createUserExpectingSuccess(username, null, DEFAULT_PASSWORD);
    }

    @Given("系統中存在帳號 {string}，密碼為 {string}")
    public void givenAccountExistsWithPassword(String username, String password) throws Exception {
        createUserExpectingSuccess(username, null, password);
    }

    @Given("系統中存在帳號 {string}，顯示名字為 {string}，密碼為 {string}")
    public void givenAccountExistsWithDisplayNameAndPassword(String username, String displayName, String password)
            throws Exception {
        createUserExpectingSuccess(username, displayName, password);
    }

    @Given("系統中已存在帳號 ID {string}，顯示名字為 {string}")
    public void givenAccountExistsWithDisplayName(String username, String displayName) throws Exception {
        createUserExpectingSuccess(username, displayName, DEFAULT_PASSWORD);
    }

    @Given("系統中不存在帳號 {string}")
    public void givenAccountDoesNotExist(String username) {
        assertFalse(userJpaRepository.existsByUsername(username));
    }

    @Given("我已登入系統，帳號為 {string}")
    public void givenLoggedIn(String username) throws Exception {
        createUserExpectingSuccess(username, null, DEFAULT_PASSWORD);
        performLogin(username, DEFAULT_PASSWORD);
        assertEquals(200, lastResult.getResponse().getStatus());
    }

    // ---- When ----

    @When("我建立一個帳號 {string}，密碼留白")
    public void whenCreateAccountWithBlankPassword(String username) throws Exception {
        attemptCreate(username, null, "");
    }

    @When("我建立一個帳號 {string}，密碼長度為 {int} 個字，包含英文大小寫與符號")
    public void whenCreateAccountWithComplexPassword(String username, int length) throws Exception {
        attemptCreate(username, null, generatePassword(length));
    }

    @When("我建立一個帳號 {string}，密碼長度為 {int} 個字")
    public void whenCreateAccountWithPasswordOfLength(String username, int length) throws Exception {
        attemptCreate(username, null, generatePassword(length));
    }

    @When("我嘗試建立另一個帳號 {string}")
    public void whenAttemptCreateDuplicateAccount(String username) throws Exception {
        attemptCreate(username, null, DEFAULT_PASSWORD);
    }

    @When("我建立一個帳號，帳號 ID 留空")
    public void whenCreateAccountWithBlankUsername() throws Exception {
        attemptCreate("", null, DEFAULT_PASSWORD);
    }

    @When("我建立一個帳號，帳號 ID 為 {string}，顯示名字為 {string}")
    public void whenCreateAccountWithUsernameAndDisplayName(String username, String displayName) throws Exception {
        attemptCreate(username, displayName, DEFAULT_PASSWORD);
    }

    @When("我建立一個新帳號，帳號 ID 為 {string}，顯示名字也為 {string}")
    public void whenCreateNewAccountWithUsernameAndDisplayName(String username, String displayName) throws Exception {
        attemptCreate(username, displayName, DEFAULT_PASSWORD);
    }

    @When("我輸入帳號 {string} 與密碼 {string}")
    public void whenEnterCredentials(String username, String password) {
        pendingLoginUsername = username;
        pendingLoginPassword = password;
    }

    @When("我輸入帳號 {string} 與任意密碼")
    public void whenEnterUsernameWithAnyPassword(String username) {
        pendingLoginUsername = username;
        pendingLoginPassword = "any-password";
    }

    @When("我送出登入表單")
    public void whenSubmitLoginForm() throws Exception {
        performLogin(pendingLoginUsername, pendingLoginPassword);
    }

    @When("我以帳號 {string} 與密碼 {string} 登入")
    public void whenLoginWithCredentials(String username, String password) throws Exception {
        performLogin(username, password);
    }

    @When("我點擊「登出」")
    public void whenClickLogout() throws Exception {
        lastResult = mockMvc.perform(post("/api/logout").session(session)).andReturn();
    }

    // ---- Then ----

    @Then("該帳號應該建立成功")
    public void thenAccountCreatedSuccessfully() {
        assertEquals(201, lastResult.getResponse().getStatus());
    }

    @Then("我應該能用空白密碼登入這個帳號")
    public void thenCanLoginWithBlankPassword() throws Exception {
        Map<?, ?> body = readBody(lastResult);
        performLogin((String) body.get("username"), "");
        assertEquals(200, lastResult.getResponse().getStatus());
    }

    @Then("系統應該顯示錯誤訊息 {string}")
    public void thenErrorMessage(String message) throws Exception {
        assertEquals(message, readBody(lastResult).get("message"));
    }

    @Then("該帳號不應該被建立")
    public void thenAccountNotCreated() {
        assertEquals(countBeforeAction, userJpaRepository.count());
    }

    @Then("不應該建立新的帳號")
    public void thenNoNewAccountCreated() {
        assertEquals(countBeforeAction, userJpaRepository.count());
    }

    @Then("該帳號的顯示名字應該是 {string}")
    public void thenDisplayNameIs(String displayName) throws Exception {
        assertEquals(displayName, readBody(lastResult).get("displayName"));
    }

    @Then("系統中應該同時存在兩個顯示名字為 {string} 的帳號，帳號 ID 分別是 {string} 與 {string}")
    public void thenTwoAccountsShareDisplayName(String displayName, String username1, String username2) {
        var first = userJpaRepository.findByUsername(username1).orElseThrow();
        var second = userJpaRepository.findByUsername(username2).orElseThrow();
        assertEquals(displayName, first.getDisplayName());
        assertEquals(displayName, second.getDisplayName());
    }

    @Then("我應該登入成功")
    public void thenLoginSuccessful() {
        assertEquals(200, lastResult.getResponse().getStatus());
    }

    @Then("TopBar 應該顯示我的名稱 {string}")
    public void thenTopBarShowsName(String name) throws Exception {
        assertEquals(name, readBody(lastResult).get("displayName"));
    }

    @Then("我應該仍停留在登入頁面")
    public void thenStillOnLoginPage() throws Exception {
        assertSessionUnauthenticated();
    }

    @Then("我應該回到登入頁面")
    public void thenBackToLoginPage() {
        assertEquals(204, lastResult.getResponse().getStatus());
    }

    @Then("我應該無法在不重新登入的情況下存取 Board")
    public void thenCannotAccessBoardWithoutLogin() throws Exception {
        assertSessionUnauthenticated();
    }

    // ---- helpers ----

    private void attemptCreate(String username, String displayName, String password) throws Exception {
        countBeforeAction = userJpaRepository.count();
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("displayName", displayName);
        body.put("password", password);
        lastResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private void createUserExpectingSuccess(String username, String displayName, String password) throws Exception {
        attemptCreate(username, displayName, password);
        assertEquals(201, lastResult.getResponse().getStatus(),
                "測試前置資料建立失敗：" + lastResult.getResponse().getContentAsString());
    }

    private void performLogin(String username, String password) throws Exception {
        Map<String, String> body = Map.of("username", username, "password", password);
        lastResult = mockMvc.perform(post("/api/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    private void assertSessionUnauthenticated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/session").session(session)).andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    private String generatePassword(int length) {
        String pattern = "Aa1!";
        StringBuilder builder = new StringBuilder();
        while (builder.length() < length) {
            builder.append(pattern);
        }
        return builder.substring(0, length);
    }
}

package io.progden.kanban.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.progden.kanban.core.domain.ItemAnchor;
import io.progden.kanban.spring.persistence.CanvasJpaEntity;
import io.progden.kanban.spring.persistence.CanvasJpaRepository;
import io.progden.kanban.spring.persistence.ItemJpaEntity;
import io.progden.kanban.spring.persistence.ItemJpaRepository;
import io.progden.kanban.spring.persistence.UserJpaRepository;
import io.progden.kanban.spring.persistence.ViewportJpaEntity;
import io.progden.kanban.spring.persistence.ViewportJpaRepository;
import java.util.ArrayList;
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
 * spec-canvas-layout.md「看板畫布初始化」「元件放置」「畫布元素排列」「畫布元素批次操作」「檢視區」
 * 對應的 Cucumber step definitions，透過 MockMvc 打 {@link io.progden.kanban.spring.web.CanvasController}
 * 的實際端點，驗證 web／application／persistence 整條路徑。
 *
 * <p>大量步驟文字只在座標／能力／錨定等修飾語不同，改用少量以 Java 正規表示式（{@code ^...$}，
 * cucumber-jvm 會自動辨識為正規表示式而非 Cucumber Expression）撰寫的共用 step definition，
 * 逐一解析每個「，」／「、」／「且」分隔的敘述子句（{@link #applyDescriptorClause}），
 * 避免為每一種措辭排列組合各寫一個方法（decision-log.md 低風險技術決定）。
 */
public class CanvasSteps {

    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CanvasJpaRepository canvasJpaRepository;

    @Autowired
    private ItemJpaRepository itemJpaRepository;

    @Autowired
    private ViewportJpaRepository viewportJpaRepository;

    private MockHttpSession session;
    private MockHttpSession ownerSession;
    private UUID currentUserId;
    private UUID currentBoardId;
    private UUID currentCanvasId;
    private MvcResult lastResult;

    private final Map<String, UUID> itemIdsByName = new HashMap<>();
    private final Map<String, Long> initialZByName = new HashMap<>();
    private final Map<String, UUID> boardIdsByName = new HashMap<>();
    private final Map<String, UUID> userIdByAlias = new HashMap<>();
    private final Map<String, String> usernameByAlias = new HashMap<>();
    private final Map<String, MockHttpSession> sessionByAlias = new HashMap<>();

    @Before
    public void resetCanvasState() {
        canvasJpaRepository.deleteAll();
        itemJpaRepository.deleteAll();
        viewportJpaRepository.deleteAll();
        session = null;
        ownerSession = null;
        currentUserId = null;
        currentBoardId = null;
        currentCanvasId = null;
        lastResult = null;
        itemIdsByName.clear();
        initialZByName.clear();
        boardIdsByName.clear();
        userIdByAlias.clear();
        usernameByAlias.clear();
        sessionByAlias.clear();
    }

    // ---- Given：看板畫布初始化前置狀態 ----

    @Given("^看板 \"([^\"]+)\" 尚無 canvas$")
    public void givenBoardHasNoCanvasYet(String boardName) throws Exception {
        ensureLoggedIn();
        currentBoardId = createBoard(boardName);
        boardIdsByName.put(boardName, currentBoardId);
    }

    @Given("^看板 \"([^\"]+)\" 已有 canvas$")
    public void givenBoardHasCanvasAlready(String boardName) throws Exception {
        ensureLoggedIn();
        currentBoardId = createBoard(boardName);
        boardIdsByName.put(boardName, currentBoardId);
        currentCanvasId = createBareCanvas(currentBoardId);
    }

    @Given("^看板 \"([^\"]+)\" 不存在$")
    public void givenBoardDoesNotExist(String boardName) throws Exception {
        ensureLoggedIn();
        boardIdsByName.put(boardName, UUID.randomUUID());
    }

    @Given("^該 canvas 存在元素 \"([^\"]+)\"，元件識別碼為 \"([^\"]+)\"，層序為 (\\d+)$")
    public void givenCanvasHasNamedItemWithComponent(String itemName, String component, long z) {
        createItem(itemName, component, ItemAnchor.CANVAS, 0, 0, 100, 100, z, true, true, true);
    }

    @Given("^該 canvas 另存在元素 \"([^\"]+)\"，層序為 (\\d+)$")
    public void givenCanvasHasAnotherItem(String itemName, long z) {
        createItem(itemName, itemName, ItemAnchor.CANVAS, 0, 0, 100, 100, z, true, true, true);
    }

    // ---- Given：元件放置／排列／批次操作共用前置狀態 ----

    @Given("^畫布已由系統建立$")
    public void givenCanvasEstablished() throws Exception {
        ensureLoggedIn();
        if (currentBoardId == null) {
            currentBoardId = createBoard("測試看板-" + UUID.randomUUID());
        }
        if (currentCanvasId == null) {
            currentCanvasId = createBareCanvas(currentBoardId);
        }
    }

    @Given("^縮放範圍為 ([\\d.]+) ～ ([\\d.]+)$")
    public void givenZoomRange(double zoomMin, double zoomMax) {
        CanvasJpaEntity entity = canvasJpaRepository.findById(currentCanvasId).orElseThrow();
        assertEquals(zoomMin, entity.getZoomMin());
        assertEquals(zoomMax, entity.getZoomMax());
    }

    @Given("^畫布中沒有任何元素$")
    public void givenCanvasHasNoItems() {
        assertTrue(itemJpaRepository.findByCanvasId(currentCanvasId).isEmpty());
    }

    @Given("^畫布中存在 (\\d+) 個錨定於畫布的元素，層序分別為 (\\d+)、(\\d+)$")
    public void givenTwoAnchoredItems(int count, long firstZ, long secondZ) {
        createItem("匿名元素-1", "匿名元素", ItemAnchor.CANVAS, 0, 0, 100, 100, firstZ, true, true, true);
        createItem("匿名元素-2", "匿名元素", ItemAnchor.CANVAS, 0, 0, 100, 100, secondZ, true, true, true);
    }

    /**
     * 「畫布中存在元素 "X"」這段文字在 spec 的 Gherkin 裡同時當 Given（建立前置資料，後面接
     * 「，」＋描述子句）與 Then（斷言先前建立的元素仍存在，不接描述子句）——Cucumber 依文字比對
     * step definition，不分 Given／Then／And 關鍵字，兩種用法只能對應同一個方法（否則會撞成
     * {@code AmbiguousStepDefinitionsException}，決策見 decision-log.md）。
     */
    @Given("^畫布中存在元素 \"([^\"]+)\"(?:，(.+))?$")
    public void givenOrThenItemExists(String itemName, String descriptor) {
        if (descriptor == null && itemIdsByName.containsKey(itemName)) {
            assertTrue(itemJpaRepository.findById(itemIdsByName.get(itemName)).isPresent());
            return;
        }
        ItemDescriptor d = new ItemDescriptor();
        if (descriptor != null) {
            applyAllClauses(d, descriptor);
        }
        long z = resolveZ(d);
        createItem(itemName, itemName, d.anchor, d.x, d.y, d.width, d.height, z, d.movable, d.resizable,
                d.removable);
    }

    // ---- Given：檢視區前置狀態 ----

    @Given("^我是使用者 \"([^\"]+)\"，我的檢視區左上角位於畫布座標 \\(([^,]+), ([^)]+)\\)，縮放比例為 ([\\d.]+)$")
    public void givenIAmUserWithViewport(String alias, double x, double y, double zoom) throws Exception {
        ensureAliasUser(alias);
        UUID userId = userIdByAlias.get(alias);
        ViewportJpaEntity entity = new ViewportJpaEntity(UUID.randomUUID(), currentCanvasId, userId);
        entity.update(x, y, zoom);
        viewportJpaRepository.save(entity);
        switchToAlias(alias);
    }

    @Given("^使用者 \"([^\"]+)\" 的檢視區左上角位於畫布座標 \\(([^,]+), ([^)]+)\\)，縮放比例為 ([\\d.]+)$")
    public void givenOtherUserHasViewport(String alias, double x, double y, double zoom) throws Exception {
        ensureAliasUser(alias);
        UUID userId = userIdByAlias.get(alias);
        ViewportJpaEntity entity = new ViewportJpaEntity(UUID.randomUUID(), currentCanvasId, userId);
        entity.update(x, y, zoom);
        viewportJpaRepository.save(entity);
    }

    @Given("^使用者 \"([^\"]+)\" 尚無檢視區$")
    public void givenUserHasNoViewport(String alias) throws Exception {
        ensureAliasUser(alias);
    }

    @Given("^我是使用者 \"([^\"]+)\"$")
    public void givenIAmUser(String alias) throws Exception {
        switchToAlias(alias);
    }

    // ---- When：看板畫布初始化 ----

    @When("^我(?:再次)?開啟看板 \"([^\"]+)\"$")
    public void whenOpenBoard(String boardName) throws Exception {
        UUID boardId = boardIdsByName.get(boardName);
        lastResult = mockMvc.perform(get("/api/boards/" + boardId + "/canvas").session(session)).andReturn();
        if (lastResult.getResponse().getStatus() == 200) {
            Map<?, ?> body = readBody(lastResult);
            currentCanvasId = UUID.fromString((String) body.get("id"));
            currentBoardId = boardId;
        }
    }

    // ---- When：元件放置 ----

    @When("^我將元件 \"([^\"]+)\" 放置到畫布，左上角 \\(([^,]+), ([^)]+)\\)，大小 ([^ ]+) × ([^ ]+)$")
    public void whenPlaceItemSimple(String component, double x, double y, double width, double height)
            throws Exception {
        doPlaceItem(component, x, y, width, height, null, null, null, null);
    }

    @When("^我將元件 \"([^\"]+)\" 放置到畫布，欄位如下：$")
    public void whenPlaceItemWithFields(String component, DataTable table) throws Exception {
        Map<String, String> fields = tableAsMap(table);
        double x = Double.parseDouble(fields.get("X"));
        double y = Double.parseDouble(fields.get("Y"));
        double width = Double.parseDouble(fields.get("寬"));
        double height = Double.parseDouble(fields.get("高"));
        String anchor = fields.containsKey("錨定") ? ("畫面".equals(fields.get("錨定")) ? "screen" : "canvas") : null;
        Boolean movable = fields.containsKey("可移動") ? !"否".equals(fields.get("可移動")) : null;
        Boolean resizable = fields.containsKey("可調整大小") ? !"否".equals(fields.get("可調整大小")) : null;
        doPlaceItem(component, x, y, width, height, anchor, movable, resizable, null);
    }

    // ---- When：畫布元素排列 ----

    @When("^我將元素 \"([^\"]+)\" 移動到左上角 \\(([^,]+), ([^)]+)\\)$")
    public void whenMoveItem(String itemName, double x, double y) throws Exception {
        UUID itemId = resolveItemIdOrRandom(itemName);
        Map<String, Object> body = Map.of("x", x, "y", y);
        lastResult = mockMvc.perform(post("/api/canvas-items/" + itemId + "/move")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我將元素 \"([^\"]+)\" 調整為左上角 \\(([^,]+), ([^)]+)\\)，大小 ([^ ]+) × ([^ ]+)$")
    public void whenResizeItem(String itemName, double x, double y, double width, double height) throws Exception {
        UUID itemId = resolveItemIdOrRandom(itemName);
        Map<String, Object> body = Map.of("x", x, "y", y, "width", width, "height", height);
        lastResult = mockMvc.perform(post("/api/canvas-items/" + itemId + "/resize")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我將元素 \"([^\"]+)\" 設為(.+)$")
    public void whenSetItemCapabilities(String itemName, String descriptor) throws Exception {
        ItemDescriptor d = new ItemDescriptor();
        applyAllClauses(d, descriptor);
        UUID itemId = resolveItemIdOrRandom(itemName);
        Map<String, Object> body = Map.of("movable", d.movable, "resizable", d.resizable, "removable", d.removable);
        lastResult = mockMvc.perform(patch("/api/canvas-items/" + itemId + "/capabilities")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我將元素 \"([^\"]+)\" (固定於畫面|錨定於畫布)，左上角為(?:畫面|畫布)座標 \\(([^,]+), ([^)]+)\\)，"
            + "大小 ([^ ]+) × ([^ ]+)$")
    public void whenSetItemAnchor(
            String itemName, String anchorText, double x, double y, double width, double height) throws Exception {
        UUID itemId = resolveItemIdOrRandom(itemName);
        String anchor = "固定於畫面".equals(anchorText) ? "screen" : "canvas";
        Map<String, Object> body = Map.of("anchor", anchor, "x", x, "y", y, "width", width, "height", height);
        lastResult = mockMvc.perform(patch("/api/canvas-items/" + itemId + "/anchor")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我將元素 \"([^\"]+)\" (置頂|置底)$")
    public void whenReorderItem(String itemName, String direction) throws Exception {
        UUID itemId = resolveItemIdOrRandom(itemName);
        Map<String, Object> body = Map.of("toFront", "置頂".equals(direction));
        lastResult = mockMvc.perform(post("/api/canvas-items/" + itemId + "/reorder")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我移除元素 \"([^\"]+)\"$")
    public void whenRemoveItem(String itemName) throws Exception {
        UUID itemId = resolveItemIdOrRandom(itemName);
        lastResult = mockMvc.perform(delete("/api/canvas-items/" + itemId).session(session)).andReturn();
    }

    // ---- When：批次操作 ----

    @When("^我將元素 \"([^\"]+)\"、\"([^\"]+)\" 一起移動，位移量為 \\(([^,]+), ([^)]+)\\)$")
    public void whenMoveItemsBatch(String firstName, String secondName, String dxText, String dyText)
            throws Exception {
        double dx = Double.parseDouble(dxText);
        double dy = Double.parseDouble(dyText);
        List<UUID> itemIds = new ArrayList<>();
        itemIds.add(resolveItemIdOrRandom(firstName));
        itemIds.add(resolveItemIdOrRandom(secondName));
        Map<String, Object> body = Map.of("itemIds", itemIds, "dx", dx, "dy", dy);
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/canvas/items/move-batch")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    @When("^我一起移除元素 \"([^\"]+)\"、\"([^\"]+)\"$")
    public void whenRemoveItemsBatch(String firstName, String secondName) throws Exception {
        List<UUID> itemIds = new ArrayList<>();
        itemIds.add(resolveItemIdOrRandom(firstName));
        itemIds.add(resolveItemIdOrRandom(secondName));
        Map<String, Object> body = Map.of("itemIds", itemIds);
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/canvas/items/remove-batch")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    // ---- When：檢視區 ----

    @When("^我將檢視區設定為左上角 \\(([^,]+), ([^)]+)\\)，縮放比例 ([\\d.]+)$")
    public void whenSetViewport(double x, double y, double zoom) throws Exception {
        Map<String, Object> body = Map.of("x", x, "y", y, "zoom", zoom);
        lastResult = mockMvc.perform(put("/api/boards/" + currentBoardId + "/canvas/viewport")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
    }

    // ---- Then：拒絕（共用） ----

    @Then("^拒絕，訊息為 \"([^\"]+)\"，且資料不變$")
    public void thenRejected(String message) throws Exception {
        int status = lastResult.getResponse().getStatus();
        assertTrue(status >= 400, "預期操作被拒絕，實際狀態碼為 " + status);
        Map<?, ?> body = readBody(lastResult);
        assertEquals(message, body.get("message"));
    }

    // ---- Then：看板畫布初始化 ----

    @Then("^該看板存在一個 canvas，縮放範圍為 ([\\d.]+) ～ ([\\d.]+)$")
    public void thenBoardHasCanvasWithZoomRange(double zoomMin, double zoomMax) throws Exception {
        Map<?, ?> body = readBody(lastResult);
        assertEquals(zoomMin, ((Number) body.get("zoomMin")).doubleValue());
        assertEquals(zoomMax, ((Number) body.get("zoomMax")).doubleValue());
        currentCanvasId = UUID.fromString((String) body.get("id"));
    }

    @Then("^該 canvas 存在一個元素，元件識別碼為 \"([^\"]+)\"$")
    public void thenCanvasHasOneItemWithComponent(String component) throws Exception {
        List<?> items = (List<?>) readBody(lastResult).get("items");
        assertEquals(1, items.size());
        Map<?, ?> item = (Map<?, ?>) items.get(0);
        assertEquals(component, item.get("component"));
    }

    @Then("^該元素的左上角位於 \\(([^,]+), ([^)]+)\\)，大小為 ([^ ]+) × ([^ ]+)$")
    public void thenTheItemAt(double x, double y, double width, double height) throws Exception {
        Map<?, ?> item = onlyItem();
        assertEquals(x, ((Number) item.get("x")).doubleValue());
        assertEquals(y, ((Number) item.get("y")).doubleValue());
        assertEquals(width, ((Number) item.get("width")).doubleValue());
        assertEquals(height, ((Number) item.get("height")).doubleValue());
    }

    @Then("^該元素錨定於畫布，層序為 (\\d+)$")
    public void thenTheItemAnchoredAtZ(long z) throws Exception {
        Map<?, ?> item = onlyItem();
        assertEquals("canvas", item.get("anchor"));
        assertEquals(z, ((Number) item.get("z")).longValue());
    }

    @Then("^該元素可移動、可調整大小且可移除$")
    public void thenTheItemFullyCapable() throws Exception {
        Map<?, ?> item = onlyItem();
        assertTrue((Boolean) item.get("movable"));
        assertTrue((Boolean) item.get("resizable"));
        assertTrue((Boolean) item.get("removable"));
    }

    @Then("^該看板仍只有一個 canvas$")
    public void thenBoardStillHasOneCanvas() {
        CanvasJpaEntity entity = canvasJpaRepository.findByBoardId(currentBoardId).orElseThrow();
        assertEquals(currentCanvasId, entity.getId());
    }

    // ---- Then：元素存在與否 ----

    @Then("^畫布中不存在元素 \"([^\"]+)\"$")
    public void thenItemNotExists(String itemName) {
        UUID id = itemIdsByName.get(itemName);
        if (id != null) {
            assertFalse(itemJpaRepository.findById(id).isPresent());
        }
    }

    // ---- Then：元素屬性 ----

    @Then("^元素 \"([^\"]+)\" 的左上角(?:仍)?位於(?:畫面座標)? \\(([^,]+), ([^)]+)\\)$")
    public void thenItemPositionIs(String itemName, double x, double y) {
        ItemJpaEntity item = loadItem(itemName);
        assertEquals(x, item.getX());
        assertEquals(y, item.getY());
    }

    @Then("^元素 \"([^\"]+)\" 的大小(?:仍)?為(?:畫面單位)? ([\\d.]+) × ([\\d.]+)$")
    public void thenItemSizeIs(String itemName, double width, double height) {
        ItemJpaEntity item = loadItem(itemName);
        assertEquals(width, item.getWidth());
        assertEquals(height, item.getHeight());
    }

    @Then("^元素 \"([^\"]+)\" (固定於畫面|錨定於畫布)$")
    public void thenItemAnchorIs(String itemName, String anchorText) {
        ItemJpaEntity item = loadItem(itemName);
        ItemAnchor expected = "固定於畫面".equals(anchorText) ? ItemAnchor.SCREEN : ItemAnchor.CANVAS;
        assertEquals(expected, item.getAnchor());
    }

    @Then("^元素 \"([^\"]+)\" 的層序(?:仍)?為 (\\d+)$")
    public void thenItemZIs(String itemName, long z) {
        ItemJpaEntity item = loadItem(itemName);
        assertEquals(z, item.getZ());
    }

    @Then("^元素 \"([^\"]+)\" 的層序不變$")
    public void thenItemZUnchanged(String itemName) {
        ItemJpaEntity item = loadItem(itemName);
        assertEquals(initialZByName.get(itemName), item.getZ());
    }

    @Then("^元素 \"([^\"]+)\" 的層序高於元素 \"([^\"]+)\"$")
    public void thenItemZHigherThan(String itemName, String otherName) {
        ItemJpaEntity item = loadItem(itemName);
        ItemJpaEntity other = loadItem(otherName);
        assertTrue(item.getZ() > other.getZ());
    }

    @Then("^元素 \"([^\"]+)\" 的層序高於所有錨定於畫布的其他元素$")
    public void thenItemZHighestAmongCanvasItems(String itemName) {
        ItemJpaEntity item = loadItem(itemName);
        boolean isHighest = itemJpaRepository.findByCanvasIdAndAnchor(currentCanvasId, ItemAnchor.CANVAS).stream()
                .filter(other -> !other.getId().equals(item.getId()))
                .allMatch(other -> item.getZ() > other.getZ());
        assertTrue(isHighest, "元素「" + itemName + "」的層序應該高於所有其他畫布元素");
    }

    @Then("^元素 \"([^\"]+)\" 的層序低於所有錨定於畫布的其他元素$")
    public void thenItemZLowestAmongCanvasItems(String itemName) {
        ItemJpaEntity item = loadItem(itemName);
        boolean isLowest = itemJpaRepository.findByCanvasIdAndAnchor(currentCanvasId, ItemAnchor.CANVAS).stream()
                .filter(other -> !other.getId().equals(item.getId()))
                .allMatch(other -> item.getZ() < other.getZ());
        assertTrue(isLowest, "元素「" + itemName + "」的層序應該低於所有其他畫布元素");
    }

    @Then("^元素 \"([^\"]+)\" ((?:不?可(?:移動|調整大小|移除))(?:[、且]不?可(?:移動|調整大小|移除))*)$")
    public void thenItemCapabilitiesAre(String itemName, String descriptor) {
        ItemDescriptor d = new ItemDescriptor();
        applyAllClauses(d, descriptor);
        ItemJpaEntity item = loadItem(itemName);
        assertEquals(d.movable, item.isMovable());
        assertEquals(d.resizable, item.isResizable());
        assertEquals(d.removable, item.isRemovable());
    }

    // ---- Then：檢視區 ----

    @Then("^我的檢視區左上角位於畫布座標 \\(([^,]+), ([^)]+)\\)$")
    public void thenMyViewportPositionIs(double x, double y) throws Exception {
        Map<?, ?> viewport = readCurrentViewport();
        assertEquals(x, ((Number) viewport.get("x")).doubleValue());
        assertEquals(y, ((Number) viewport.get("y")).doubleValue());
    }

    @Then("^我的檢視區的縮放比例(?:仍)?為 ([\\d.]+)$")
    public void thenMyViewportZoomIs(double zoom) throws Exception {
        Map<?, ?> viewport = readCurrentViewport();
        assertEquals(zoom, ((Number) viewport.get("zoom")).doubleValue());
    }

    @Then("^使用者 \"([^\"]+)\" 的檢視區左上角仍位於畫布座標 \\(([^,]+), ([^)]+)\\)$")
    public void thenOtherUserViewportPositionUnchanged(String alias, double x, double y) {
        ViewportJpaEntity viewport = loadViewport(alias);
        assertEquals(x, viewport.getX());
        assertEquals(y, viewport.getY());
    }

    @Then("^使用者 \"([^\"]+)\" 的檢視區的縮放比例仍為 ([\\d.]+)$")
    public void thenOtherUserViewportZoomUnchanged(String alias, double zoom) {
        ViewportJpaEntity viewport = loadViewport(alias);
        assertEquals(zoom, viewport.getZoom());
    }

    // ---- helpers：使用者與看板 ----

    private void ensureLoggedIn() throws Exception {
        if (currentUserId != null) {
            return;
        }
        String username = "canvas-owner-" + UUID.randomUUID();
        session = registerAndLogin(username);
        ownerSession = session;
        currentUserId = userJpaRepository.findByUsername(username).orElseThrow().getId();
    }

    private MockHttpSession registerAndLogin(String username) throws Exception {
        MockHttpSession newSession = new MockHttpSession();
        Map<String, String> body = Map.of("username", username, "password", PASSWORD);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .session(newSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(200, loginResult.getResponse().getStatus());
        return newSession;
    }

    private UUID createBoard(String name) throws Exception {
        Map<String, String> body = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
        return UUID.fromString((String) readBody(result).get("id"));
    }

    private UUID createBareCanvas(UUID boardId) {
        CanvasJpaEntity entity = new CanvasJpaEntity(UUID.randomUUID(), boardId, 0.1, 4);
        canvasJpaRepository.save(entity);
        return entity.getId();
    }

    private void ensureAliasUser(String alias) throws Exception {
        if (userIdByAlias.containsKey(alias)) {
            return;
        }
        String username = "canvas-" + alias + "-" + UUID.randomUUID();
        Map<String, String> body = Map.of("username", username, "password", PASSWORD);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        UUID userId = userJpaRepository.findByUsername(username).orElseThrow().getId();
        userIdByAlias.put(alias, userId);
        usernameByAlias.put(alias, username);
        Map<String, String> inviteBody = Map.of("username", username, "role", "MEMBER");
        mockMvc.perform(post("/api/boards/" + currentBoardId + "/members")
                .session(ownerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteBody)));
    }

    private void switchToAlias(String alias) throws Exception {
        MockHttpSession aliasSession = sessionByAlias.get(alias);
        if (aliasSession == null) {
            aliasSession = registerAndLogin(usernameByAlias.get(alias));
            sessionByAlias.put(alias, aliasSession);
        }
        session = aliasSession;
        currentUserId = userIdByAlias.get(alias);
    }

    // ---- helpers：畫布元素 ----

    private UUID createItem(String itemName, String component, ItemAnchor anchor, double x, double y, double width,
            double height, long z, boolean movable, boolean resizable, boolean removable) {
        ItemJpaEntity entity = new ItemJpaEntity(UUID.randomUUID(), currentCanvasId);
        entity.update(component, anchor, x, y, width, height, z, movable, resizable, removable);
        itemJpaRepository.save(entity);
        itemIdsByName.put(itemName, entity.getId());
        initialZByName.put(itemName, z);
        return entity.getId();
    }

    private UUID resolveItemIdOrRandom(String itemName) {
        return itemIdsByName.getOrDefault(itemName, UUID.randomUUID());
    }

    private ItemJpaEntity loadItem(String itemName) {
        return itemJpaRepository.findById(itemIdsByName.get(itemName)).orElseThrow();
    }

    private void doPlaceItem(String component, double x, double y, double width, double height, String anchor,
            Boolean movable, Boolean resizable, Boolean removable) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("component", component);
        body.put("x", x);
        body.put("y", y);
        body.put("width", width);
        body.put("height", height);
        body.put("anchor", anchor);
        body.put("movable", movable);
        body.put("resizable", resizable);
        body.put("removable", removable);
        lastResult = mockMvc.perform(post("/api/boards/" + currentBoardId + "/canvas/items")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        if (lastResult.getResponse().getStatus() == 201) {
            Map<?, ?> responseBody = readBody(lastResult);
            UUID itemId = UUID.fromString((String) responseBody.get("id"));
            itemIdsByName.put(component, itemId);
            initialZByName.put(component, ((Number) responseBody.get("z")).longValue());
        }
    }

    private long resolveZ(ItemDescriptor d) {
        if (d.zLowerThan != null) {
            return loadItem(d.zLowerThan).getZ() - 1;
        }
        if (d.zHigherThan != null) {
            return loadItem(d.zHigherThan).getZ() + 1;
        }
        if (d.z != -1) {
            return d.z;
        }
        return itemJpaRepository.findByCanvasIdAndAnchor(currentCanvasId, d.anchor).stream()
                .mapToLong(ItemJpaEntity::getZ).max().orElse(0) + 1;
    }

    // ---- helpers：檢視區 ----

    private Map<?, ?> readCurrentViewport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/" + currentBoardId + "/canvas").session(session))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        return (Map<?, ?>) readBody(result).get("viewport");
    }

    private ViewportJpaEntity loadViewport(String alias) {
        UUID userId = userIdByAlias.get(alias);
        return viewportJpaRepository.findByCanvasIdAndUserId(currentCanvasId, userId).orElseThrow();
    }

    // ---- helpers：共用解析 ----

    private Map<?, ?> onlyItem() throws Exception {
        List<?> items = (List<?>) readBody(lastResult).get("items");
        assertEquals(1, items.size());
        return (Map<?, ?>) items.get(0);
    }

    private Map<String, String> tableAsMap(DataTable table) {
        List<List<String>> rows = table.asLists(String.class);
        Map<String, String> fields = new HashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            fields.put(rows.get(i).get(0), rows.get(i).get(1));
        }
        return fields;
    }

    private Map<?, ?> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    /**
     * 解析「畫布中存在元素」「元素設為」等敘述句的子句，逐一以「，」拆開後，每個子句再以「、」／「且」
     * 拆開為最小 token（見類別註解），套用到同一個描述子上。
     */
    private void applyAllClauses(ItemDescriptor descriptor, String text) {
        for (String clause : text.split("，")) {
            for (String token : clause.split("[、且]")) {
                applyDescriptorClause(descriptor, token.trim());
            }
        }
    }

    private void applyDescriptorClause(ItemDescriptor d, String clause) {
        if (clause.isEmpty()) {
            return;
        }
        if (clause.equals("可移動")) {
            d.movable = true;
        } else if (clause.equals("不可移動")) {
            d.movable = false;
        } else if (clause.equals("可調整大小")) {
            d.resizable = true;
        } else if (clause.equals("不可調整大小")) {
            d.resizable = false;
        } else if (clause.equals("可移除")) {
            d.removable = true;
        } else if (clause.equals("不可移除")) {
            d.removable = false;
        } else if (clause.equals("錨定於畫布")) {
            d.anchor = ItemAnchor.CANVAS;
        } else if (clause.equals("固定於畫面")) {
            d.anchor = ItemAnchor.SCREEN;
        } else if (clause.matches("層序為 \\d+")) {
            d.z = Long.parseLong(clause.replaceAll("[^\\d]", ""));
        } else if (clause.matches("層序低於元素 \"[^\"]+\"")) {
            d.zLowerThan = clause.replaceAll(".*\"([^\"]+)\".*", "$1");
        } else if (clause.matches("層序高於元素 \"[^\"]+\"")) {
            d.zHigherThan = clause.replaceAll(".*\"([^\"]+)\".*", "$1");
        } else if (clause.matches("左上角(?:為畫面座標|為畫布座標)? \\([^,]+, [^)]+\\)")) {
            String[] coords = clause.replaceAll(".*\\(([^,]+), ([^)]+)\\).*", "$1,$2").split(",");
            d.x = Double.parseDouble(coords[0].trim());
            d.y = Double.parseDouble(coords[1].trim());
        } else if (clause.matches("大小 [\\d.]+ × [\\d.]+")) {
            String[] size = clause.replaceAll("大小 ([\\d.]+) × ([\\d.]+)", "$1,$2").split(",");
            d.width = Double.parseDouble(size[0]);
            d.height = Double.parseDouble(size[1]);
        } else {
            throw new IllegalArgumentException("無法解析的畫布元素描述子句：「" + clause + "」");
        }
    }

    /** 畫布元素敘述句解析用的暫存值物件，預設值對應 spec 欄位定義的預設值。 */
    private static final class ItemDescriptor {
        ItemAnchor anchor = ItemAnchor.CANVAS;
        double x = 0;
        double y = 0;
        double width = 100;
        double height = 100;
        long z = -1;
        String zLowerThan;
        String zHigherThan;
        boolean movable = true;
        boolean resizable = true;
        boolean removable = true;
    }
}

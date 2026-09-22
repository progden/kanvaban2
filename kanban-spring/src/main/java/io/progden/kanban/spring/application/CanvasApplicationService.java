package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Canvas;
import io.progden.kanban.core.domain.CanvasRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.Item;
import io.progden.kanban.core.domain.ItemAnchor;
import io.progden.kanban.core.domain.ItemRepository;
import io.progden.kanban.core.domain.Viewport;
import io.progden.kanban.core.domain.ViewportRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-canvas-layout.md「看板畫布初始化」「元件放置」「畫布元素排列」「畫布元素批次操作」「檢視區」
 * 對應的 application 層。
 *
 * <p>權限檢查：{@code r-canvas-editor}（{@code uc-place-item}…{@code uc-remove-items}，除
 * {@code uc-init-canvas}／{@code uc-set-viewport} 外）對應 F02 的 Owner／Member，沿用既有
 * {@link BoardMembershipApplicationService#ensureCanEdit} 統一實作；{@code r-canvas-viewer}
 * （{@code uc-init-canvas}／{@code uc-set-viewport}，任何成員含 Viewer 皆可）沿用
 * {@link BoardMembershipApplicationService#ensureMember}（2026-09-18 人工決議角色對應關係，見
 * spec-canvas-layout.md 變更紀錄）。
 *
 * <p>{@code item.z} 的「同一錨定方式現有元素最大 z 值加一」（{@code uc-place-item}／
 * {@code uc-set-item-anchor}）與置頂／置底（{@code uc-reorder-item}）都在這裡計算後傳給
 * {@code Item} 的方法，{@code Item} 本身不查詢其他 {@code item}（見 domain 類別註解）。
 */
@Service
public class CanvasApplicationService {

    private static final String BOARD_NOT_FOUND_MESSAGE = "看板不存在";
    private static final String ITEM_NOT_FOUND_MESSAGE = "畫布元素不存在";

    private final CanvasRepository canvasRepository;
    private final ItemRepository itemRepository;
    private final ViewportRepository viewportRepository;
    private final BoardRepository boardRepository;
    private final BoardMembershipApplicationService boardMembershipApplicationService;

    public CanvasApplicationService(CanvasRepository canvasRepository, ItemRepository itemRepository,
            ViewportRepository viewportRepository, BoardRepository boardRepository,
            BoardMembershipApplicationService boardMembershipApplicationService) {
        this.canvasRepository = canvasRepository;
        this.itemRepository = itemRepository;
        this.viewportRepository = viewportRepository;
        this.boardRepository = boardRepository;
        this.boardMembershipApplicationService = boardMembershipApplicationService;
    }

    /**
     * {@code uc-init-canvas}：開啟看板時初始化畫布與看板本體 item；{@code canvas}／看板本體 item
     * 原本已存在時不重複建立（post 第 3 條）。
     */
    @Transactional
    public Canvas initCanvas(UUID boardId, UUID operatorId) {
        if (boardRepository.findById(boardId).isEmpty()) {
            throw new DomainException(ErrorCode.BOARD_NOT_FOUND, BOARD_NOT_FOUND_MESSAGE);
        }
        boardMembershipApplicationService.ensureMember(boardId, operatorId);
        Canvas canvas = canvasRepository.findByBoardId(boardId).orElse(null);
        boolean canvasCreated = canvas == null;
        if (canvasCreated) {
            canvas = Canvas.createFor(boardId);
            canvasRepository.save(canvas);
        }
        if (canvasCreated || itemRepository.findByCanvasId(canvas.getId()).isEmpty()) {
            Item boardItem = Item.place(canvas.getId(), "board", ItemAnchor.CANVAS, 0, 0, 900, 600, 1, true, true,
                    true);
            itemRepository.save(boardItem);
        }
        return canvas;
    }

    public Canvas getCanvas(UUID boardId, UUID operatorId) {
        boardMembershipApplicationService.ensureMember(boardId, operatorId);
        return loadCanvasByBoard(boardId);
    }

    public List<Item> listItems(UUID canvasId) {
        return itemRepository.findByCanvasId(canvasId).stream()
                .sorted(Comparator.comparing(Item::getAnchor).thenComparingLong(Item::getZ))
                .toList();
    }

    public Viewport getViewport(UUID canvasId, UUID userId) {
        return viewportRepository.findByCanvasIdAndUserId(canvasId, userId).orElse(null);
    }

    /** {@code uc-place-item}：{@code item.z} 為同一錨定方式現有元素最大 z 值加一，尚無元素時為 1。 */
    @Transactional
    public Item placeItem(UUID boardId, UUID operatorId, String component, double x, double y, double width,
            double height, ItemAnchor anchor, boolean movable, boolean resizable, boolean removable) {
        boardMembershipApplicationService.ensureCanEdit(boardId, operatorId);
        Canvas canvas = loadCanvasByBoard(boardId);
        ItemAnchor resolvedAnchor = anchor == null ? ItemAnchor.CANVAS : anchor;
        long z = nextZ(canvas.getId(), resolvedAnchor);
        Item item = Item.place(canvas.getId(), component, resolvedAnchor, x, y, width, height, z, movable, resizable,
                removable);
        itemRepository.save(item);
        return item;
    }

    /** {@code uc-remove-item}。 */
    @Transactional
    public void removeItem(UUID itemId, UUID operatorId) {
        Item item = loadItem(itemId);
        boardMembershipApplicationService.ensureCanEdit(boardIdOf(item), operatorId);
        item.ensureRemovable();
        itemRepository.deleteById(itemId);
    }

    /** {@code uc-move-item}。 */
    @Transactional
    public Item moveItem(UUID itemId, UUID operatorId, double x, double y) {
        Item item = loadItem(itemId);
        boardMembershipApplicationService.ensureCanEdit(boardIdOf(item), operatorId);
        item.move(x, y);
        itemRepository.save(item);
        return item;
    }

    /** {@code uc-resize-item}。 */
    @Transactional
    public Item resizeItem(UUID itemId, UUID operatorId, double x, double y, double width, double height) {
        Item item = loadItem(itemId);
        boardMembershipApplicationService.ensureCanEdit(boardIdOf(item), operatorId);
        item.resize(x, y, width, height);
        itemRepository.save(item);
        return item;
    }

    /** {@code uc-set-item-capabilities}。 */
    @Transactional
    public Item setItemCapabilities(UUID itemId, UUID operatorId, boolean movable, boolean resizable,
            boolean removable) {
        Item item = loadItem(itemId);
        boardMembershipApplicationService.ensureCanEdit(boardIdOf(item), operatorId);
        item.setCapabilities(movable, resizable, removable);
        itemRepository.save(item);
        return item;
    }

    /**
     * {@code uc-set-item-anchor}：新座標／大小由呼叫端（web 層依請求）以新錨定座標系解讀後傳入，
     * 本方法不換算（post 第 2 條）；新的 {@code item.z} 為新錨定方式現有元素最大 z 值加一。
     */
    @Transactional
    public Item setItemAnchor(UUID itemId, UUID operatorId, ItemAnchor newAnchor, double x, double y, double width,
            double height) {
        Item item = loadItem(itemId);
        UUID boardId = boardIdOf(item);
        boardMembershipApplicationService.ensureCanEdit(boardId, operatorId);
        long z = nextZ(item.getCanvasId(), newAnchor);
        item.changeAnchor(newAnchor, x, y, width, height, z);
        itemRepository.save(item);
        return item;
    }

    /** {@code uc-reorder-item}：置頂或置底。 */
    @Transactional
    public Item reorderItem(UUID itemId, UUID operatorId, boolean toFront) {
        Item item = loadItem(itemId);
        boardMembershipApplicationService.ensureCanEdit(boardIdOf(item), operatorId);
        List<Item> siblings = itemRepository.findByCanvasIdAndAnchor(item.getCanvasId(), item.getAnchor()).stream()
                .filter(other -> !other.getId().equals(itemId))
                .toList();
        long newZ;
        if (toFront) {
            newZ = siblings.stream().mapToLong(Item::getZ).max().orElse(item.getZ() - 1) + 1;
        } else {
            newZ = siblings.stream().mapToLong(Item::getZ).min().orElse(item.getZ() + 1) - 1;
        }
        item.reorderTo(newZ);
        itemRepository.save(item);
        return item;
    }

    /**
     * {@code uc-move-items}：批次移動，全成功或全失敗——先對指定的每個 {@code item} 逐一驗證存在、
     * 錨定方式一致、可移動，全部通過才逐一套用位移量並存回。
     */
    @Transactional
    public List<Item> moveItems(UUID boardId, UUID operatorId, List<UUID> itemIds, double dx, double dy) {
        boardMembershipApplicationService.ensureCanEdit(boardId, operatorId);
        List<Item> items = loadItemsInBoard(boardId, itemIds);
        ItemAnchor firstAnchor = items.get(0).getAnchor();
        boolean mixedAnchor = items.stream().anyMatch(item -> item.getAnchor() != firstAnchor);
        if (mixedAnchor) {
            throw new DomainException(
                    ErrorCode.MIXED_ITEM_ANCHOR_IN_BATCH, "不可同時移動畫布元素與畫面固定元素");
        }
        items.forEach(item -> item.ensureMovable("所選元素中有不可移動的元素"));
        items.forEach(item -> item.translate(dx, dy));
        items.forEach(itemRepository::save);
        return items;
    }

    /**
     * {@code uc-remove-items}：批次移除，全成功或全失敗——先對指定的每個 {@code item} 逐一驗證存在、
     * 可移除，全部通過才逐一刪除。
     */
    @Transactional
    public void removeItems(UUID boardId, UUID operatorId, List<UUID> itemIds) {
        boardMembershipApplicationService.ensureCanEdit(boardId, operatorId);
        List<Item> items = loadItemsInBoard(boardId, itemIds);
        items.forEach(item -> item.ensureRemovable("所選元素中有不可移除的元素"));
        items.forEach(item -> itemRepository.deleteById(item.getId()));
    }

    /** {@code uc-set-viewport}：操作者本人的檢視區，第一次設定時建立。 */
    @Transactional
    public Viewport setViewport(UUID boardId, UUID operatorId, double x, double y, double zoom) {
        boardMembershipApplicationService.ensureMember(boardId, operatorId);
        Canvas canvas = loadCanvasByBoard(boardId);
        canvas.ensureZoomInRange(zoom);
        Viewport viewport = viewportRepository.findByCanvasIdAndUserId(canvas.getId(), operatorId)
                .orElseGet(() -> Viewport.createFor(canvas.getId(), operatorId, x, y, zoom));
        viewport.update(x, y, zoom);
        viewportRepository.save(viewport);
        return viewport;
    }

    private long nextZ(UUID canvasId, ItemAnchor anchor) {
        return itemRepository.findByCanvasIdAndAnchor(canvasId, anchor).stream()
                .mapToLong(Item::getZ).max().orElse(0) + 1;
    }

    private List<Item> loadItemsInBoard(UUID boardId, List<UUID> itemIds) {
        Canvas canvas = loadCanvasByBoard(boardId);
        return itemIds.stream()
                .map(itemId -> itemRepository.findById(itemId)
                        .filter(item -> item.getCanvasId().equals(canvas.getId()))
                        .orElseThrow(() -> new DomainException(ErrorCode.CANVAS_ITEM_NOT_FOUND,
                                ITEM_NOT_FOUND_MESSAGE)))
                .toList();
    }

    private Item loadItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new DomainException(ErrorCode.CANVAS_ITEM_NOT_FOUND, ITEM_NOT_FOUND_MESSAGE));
    }

    private UUID boardIdOf(Item item) {
        return canvasRepository.findById(item.getCanvasId())
                .orElseThrow(() -> new DomainException(ErrorCode.CANVAS_NOT_FOUND, "找不到指定的畫布"))
                .getBoardId();
    }

    private Canvas loadCanvasByBoard(UUID boardId) {
        return canvasRepository.findByBoardId(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.CANVAS_NOT_FOUND, "看板尚未初始化畫布"));
    }
}

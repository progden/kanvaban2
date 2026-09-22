package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Canvas;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.Item;
import io.progden.kanban.core.domain.ItemAnchor;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.core.domain.Viewport;
import io.progden.kanban.spring.application.CanvasApplicationService;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-canvas-layout.md「看板畫布初始化」「元件放置」「畫布元素排列」「畫布元素批次操作」「檢視區」
 * 對應的 web 層。
 *
 * <p>開啟看板（{@code GET /api/boards/{boardId}/canvas}）觸發 {@code uc-init-canvas}，回傳整個畫布、
 * 全部元素與操作者自己的檢視區。單一元素操作掛在 {@code /api/canvas-items/{itemId}}（Item 是獨立
 * Aggregate，比照 {@code CardController} 對 Card 的慣例，見該類別註解）；批次操作與檢視區設定需要
 * {@code boardId} 才能定位 canvas，掛在 {@code /api/boards/{boardId}/canvas/...}。
 */
@RestController
public class CanvasController {

    private final CanvasApplicationService canvasApplicationService;
    private final UserRepository userRepository;

    public CanvasController(CanvasApplicationService canvasApplicationService, UserRepository userRepository) {
        this.canvasApplicationService = canvasApplicationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/boards/{boardId}/canvas")
    public ResponseEntity<?> openCanvas(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            Canvas canvas = canvasApplicationService.initCanvas(boardId, operatorId);
            return ResponseEntity.ok(toCanvasResponse(canvas, operatorId));
        });
    }

    @PostMapping("/api/boards/{boardId}/canvas/items")
    public ResponseEntity<?> placeItem(
            @PathVariable UUID boardId, @RequestBody PlaceItemRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.placeItem(boardId, operatorId, request.component(), request.x(),
                    request.y(), request.width(), request.height(), parseAnchor(request.anchor()),
                    request.movableOrDefault(), request.resizableOrDefault(), request.removableOrDefault());
            return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(item));
        });
    }

    @DeleteMapping("/api/canvas-items/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable UUID itemId, HttpSession session) {
        return withOperator(session, operatorId -> {
            canvasApplicationService.removeItem(itemId, operatorId);
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/api/canvas-items/{itemId}/move")
    public ResponseEntity<?> moveItem(
            @PathVariable UUID itemId, @RequestBody MoveItemRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.moveItem(itemId, operatorId, request.x(), request.y());
            return ResponseEntity.ok(ItemResponse.from(item));
        });
    }

    @PostMapping("/api/canvas-items/{itemId}/resize")
    public ResponseEntity<?> resizeItem(
            @PathVariable UUID itemId, @RequestBody ResizeItemRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.resizeItem(
                    itemId, operatorId, request.x(), request.y(), request.width(), request.height());
            return ResponseEntity.ok(ItemResponse.from(item));
        });
    }

    @PatchMapping("/api/canvas-items/{itemId}/capabilities")
    public ResponseEntity<?> setItemCapabilities(
            @PathVariable UUID itemId, @RequestBody SetItemCapabilitiesRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.setItemCapabilities(
                    itemId, operatorId, request.movable(), request.resizable(), request.removable());
            return ResponseEntity.ok(ItemResponse.from(item));
        });
    }

    @PatchMapping("/api/canvas-items/{itemId}/anchor")
    public ResponseEntity<?> setItemAnchor(
            @PathVariable UUID itemId, @RequestBody SetItemAnchorRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.setItemAnchor(itemId, operatorId, parseAnchor(request.anchor()),
                    request.x(), request.y(), request.width(), request.height());
            return ResponseEntity.ok(ItemResponse.from(item));
        });
    }

    @PostMapping("/api/canvas-items/{itemId}/reorder")
    public ResponseEntity<?> reorderItem(
            @PathVariable UUID itemId, @RequestBody ReorderItemRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Item item = canvasApplicationService.reorderItem(itemId, operatorId, request.toFront());
            return ResponseEntity.ok(ItemResponse.from(item));
        });
    }

    @PostMapping("/api/boards/{boardId}/canvas/items/move-batch")
    public ResponseEntity<?> moveItems(
            @PathVariable UUID boardId, @RequestBody MoveItemsRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            var items = canvasApplicationService.moveItems(
                    boardId, operatorId, request.itemIds(), request.dx(), request.dy());
            return ResponseEntity.ok(items.stream().map(ItemResponse::from).toList());
        });
    }

    @PostMapping("/api/boards/{boardId}/canvas/items/remove-batch")
    public ResponseEntity<?> removeItems(
            @PathVariable UUID boardId, @RequestBody RemoveItemsRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            canvasApplicationService.removeItems(boardId, operatorId, request.itemIds());
            return ResponseEntity.noContent().build();
        });
    }

    @PutMapping("/api/boards/{boardId}/canvas/viewport")
    public ResponseEntity<?> setViewport(
            @PathVariable UUID boardId, @RequestBody SetViewportRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Viewport viewport = canvasApplicationService.setViewport(
                    boardId, operatorId, request.x(), request.y(), request.zoom());
            return ResponseEntity.ok(ViewportResponse.from(viewport));
        });
    }

    private CanvasResponse toCanvasResponse(Canvas canvas, UUID operatorId) {
        var items = canvasApplicationService.listItems(canvas.getId()).stream().map(ItemResponse::from).toList();
        Viewport viewport = canvasApplicationService.getViewport(canvas.getId(), operatorId);
        return CanvasResponse.of(canvas, items, viewport == null ? null : ViewportResponse.from(viewport));
    }

    private ItemAnchor parseAnchor(String anchor) {
        if (anchor == null) {
            return null;
        }
        return switch (anchor) {
            case "canvas" -> ItemAnchor.CANVAS;
            case "screen" -> ItemAnchor.SCREEN;
            default -> throw new DomainException(ErrorCode.INVALID_ITEM_ANCHOR, "無效的錨定方式");
        };
    }

    private ResponseEntity<?> withOperator(HttpSession session, Function<UUID, ResponseEntity<?>> action) {
        Object username = session.getAttribute(UserController.SESSION_USERNAME_ATTRIBUTE);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        User operator = userRepository.findByUsername((String) username).orElse(null);
        if (operator == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        try {
            return action.apply(operator.getId());
        } catch (DomainException e) {
            return ResponseEntity.status(statusFor(e.getCode())).body(new ErrorResponse(e.getMessage()));
        }
    }

    private HttpStatus statusFor(ErrorCode code) {
        return switch (code) {
            case INVALID_ITEM_SIZE, INVALID_ITEM_ANCHOR, MIXED_ITEM_ANCHOR_IN_BATCH, VIEWPORT_ZOOM_OUT_OF_RANGE ->
                    HttpStatus.BAD_REQUEST;
            case BOARD_NOT_FOUND, CANVAS_NOT_FOUND, CANVAS_ITEM_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CANVAS_ITEM_NOT_MOVABLE, CANVAS_ITEM_NOT_RESIZABLE, CANVAS_ITEM_NOT_REMOVABLE ->
                    HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}

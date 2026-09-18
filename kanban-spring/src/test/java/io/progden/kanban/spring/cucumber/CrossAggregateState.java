package io.progden.kanban.spring.cucumber;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * spec-kanban-basic.md 有 3 段 Gherkin 步驟文字同時出現在「Swimlane 管理」與「Card（卡片）編輯」
 * 兩個 Feature（「我確認新增」「我確認刪除」「該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間」）；
 * Cucumber 同一段文字只能對應一個 step definition，{@link BoardSteps}／{@link CardSteps} 用這個
 * Spring bean 暫存「這次操作實際要提交到哪個 Aggregate」，讓共用文字的 step definition 可以正確分流。
 *
 * <p>採 Spring 單例（跨整個測試套件），因此每個欄位用畢即由呼叫端清空，不依賴 {@code @Before} 重置。
 */
@Component
class CrossAggregateState {

    // ---- 「我確認新增」：新增卡片待送出的暫存輸入 ----
    private boolean addingCard;
    private UUID pendingCardSwimlaneId;
    private UUID pendingCardStageId;
    private String pendingCardTitle;

    // ---- 「我確認刪除」：待刪除卡片 id（非 null 代表這次確認刪除是刪卡片，不是刪 Swimlane） ----
    private UUID pendingDeleteCardId;

    // ---- 「該操作應該被記錄為一筆活動紀錄」：上一筆要驗證的卡片活動紀錄；null 代表驗證 board ----
    private UUID lastCardActivityId;

    void reset() {
        clearAddCard();
        clearPendingDeleteCard();
        lastActivityOnBoard();
    }

    void beginAddCard(UUID swimlaneId, UUID stageId) {
        this.addingCard = true;
        this.pendingCardSwimlaneId = swimlaneId;
        this.pendingCardStageId = stageId;
    }

    void setPendingCardTitle(String title) {
        this.pendingCardTitle = title;
    }

    boolean isAddingCard() {
        return addingCard;
    }

    UUID pendingCardSwimlaneId() {
        return pendingCardSwimlaneId;
    }

    UUID pendingCardStageId() {
        return pendingCardStageId;
    }

    String pendingCardTitle() {
        return pendingCardTitle;
    }

    void clearAddCard() {
        this.addingCard = false;
        this.pendingCardSwimlaneId = null;
        this.pendingCardStageId = null;
        this.pendingCardTitle = null;
    }

    void pendingDeleteCard(UUID cardId) {
        this.pendingDeleteCardId = cardId;
    }

    UUID pendingDeleteCardId() {
        return pendingDeleteCardId;
    }

    void clearPendingDeleteCard() {
        this.pendingDeleteCardId = null;
    }

    void lastActivityOnCard(UUID cardId) {
        this.lastCardActivityId = cardId;
    }

    void lastActivityOnBoard() {
        this.lastCardActivityId = null;
    }

    UUID lastCardActivityId() {
        return lastCardActivityId;
    }
}

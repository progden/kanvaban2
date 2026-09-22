package io.progden.kanban.core.domain;

/**
 * {@code item.anchor}：畫布元素的錨定方式（spec-canvas-layout.md 欄位定義）。
 * CANVAS 表示隨畫布平移縮放；SCREEN 表示固定在畫面上。
 */
public enum ItemAnchor {
    CANVAS,
    SCREEN
}

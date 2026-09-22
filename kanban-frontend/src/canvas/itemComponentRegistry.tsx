// spec-canvas-layout.md「item.component 為元件本體的識別碼，本模組不解讀其內容」：
// s-canvas 只負責把 item 的位置／大小／層序算出來，元件本體內容由所屬模組定義。
// 這裡提供一個最小掛載點，讓之後的前端任務（T-14 起）用 registerItemComponent 掛上自己的內容，
// 目前只有 F01 看板本體（item.component === 'board'，見 uc-init-canvas）會被建立，
// T-14 尚未實作前顯示預留位置。
import type { ComponentType } from 'react';

export interface ItemContentProps {
  itemId: string;
  component: string;
  width: number;
  height: number;
  boardId: string;
}

interface RegistryEntry {
  renderer: ComponentType<ItemContentProps>;
  label: string;
}

const registry = new Map<string, RegistryEntry>();

// label 只給「加入元件」對話框的下拉選單顯示用，不影響 item.component 實際存的值；
// 沒傳就沿用 component 識別碼本身（見 registerItemComponent 呼叫端）。
export function registerItemComponent(
  component: string,
  renderer: ComponentType<ItemContentProps>,
  label?: string,
): void {
  registry.set(component, { renderer, label: label ?? component });
}

function PlaceholderContent({ component }: ItemContentProps) {
  return <div className="canvas-item__placeholder">元件「{component}」內容待其所屬模組實作</div>;
}

export function resolveItemComponent(component: string): ComponentType<ItemContentProps> {
  return registry.get(component)?.renderer ?? PlaceholderContent;
}

export interface RegisteredComponentOption {
  component: string;
  label: string;
}

// 給「加入元件」對話框（uc-place-item）當下拉選單的資料來源：可加入的元件就是已經掛上內容的
// 這些，識別碼本身沒有 spec 定義的固定清單（見 spec-canvas-layout.md 待釐清），與其讓使用者手打
// 容易打錯，不如直接列出目前系統認得的這些。exclude 用來排除像 "board" 這種由 uc-init-canvas
// 自動建立、不該讓使用者手動再加一個的識別碼。
export function listRegisteredComponents(exclude: string[] = []): RegisteredComponentOption[] {
  const excluded = new Set(exclude);
  return [...registry.entries()]
    .filter(([component]) => !excluded.has(component))
    .map(([component, entry]) => ({ component, label: entry.label }))
    .sort((a, b) => a.label.localeCompare(b.label, 'zh-Hant'));
}

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

const registry = new Map<string, ComponentType<ItemContentProps>>();

export function registerItemComponent(component: string, renderer: ComponentType<ItemContentProps>): void {
  registry.set(component, renderer);
}

function PlaceholderContent({ component }: ItemContentProps) {
  return <div className="canvas-item__placeholder">元件「{component}」內容待其所屬模組實作</div>;
}

export function resolveItemComponent(component: string): ComponentType<ItemContentProps> {
  return registry.get(component) ?? PlaceholderContent;
}

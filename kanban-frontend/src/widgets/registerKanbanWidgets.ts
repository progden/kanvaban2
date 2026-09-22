// 把 F03 四個儀表板掛上 canvas/itemComponentRegistry.tsx 提供的掛載點（見該檔案 T-13 留下的說明：
// 「之後的前端任務用 registerItemComponent 掛上自己的內容」）。
//
// item.component 具體字串值 spec 未定義：spec-canvas-layout.md「待釐清」與 ui-kanban-widgets.md
// 「進入與離開」段都指出 F03 這四個 item 如何新增、component 值為何，待整合 CR 定案（OQ-49）。
// 這裡先採用與 F03 Screen ID 相同的字串（s-cycle-lead-time-dashboard 等）作為 component 值，
// 因為這是規格裡唯一已存在、不會與其他模組衝突的識別碼；正式值待 OQ-49 對應的整合 CR 定案後再改。
import { registerItemComponent } from '../canvas/itemComponentRegistry';
import { CycleLeadTimeDashboardWidget } from './CycleLeadTimeDashboardWidget';
import { WipDashboardWidget } from './WipDashboardWidget';
import { ThroughputCfdDashboardWidget } from './ThroughputCfdDashboardWidget';
import { DueDateReminderWidget } from './DueDateReminderWidget';

export function registerKanbanWidgets(): void {
  registerItemComponent('s-cycle-lead-time-dashboard', CycleLeadTimeDashboardWidget);
  registerItemComponent('s-wip-dashboard', WipDashboardWidget);
  registerItemComponent('s-throughput-cfd-dashboard', ThroughputCfdDashboardWidget);
  registerItemComponent('s-duedate-reminder', DueDateReminderWidget);
}

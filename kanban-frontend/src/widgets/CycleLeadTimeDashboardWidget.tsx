// s-cycle-lead-time-dashboard：依 ui-kanban-widgets.md 操作表，開啟畫面即觸發 uc-view-cycle-lead-time。
// 平均值欄位是 ui-kanban-widgets.md「資料」段要求的顯示內容，但 uc-view-cycle-lead-time post 與後端
// PercentileView 都只定義 P50/P85/P95；平均值改由前端用回應中每張卡片的 Lead/Cycle Time 自行平均
// （不排除 Cycle Time 為「無」的卡片之外的資料本來就都來自這支 API，屬單純衍生計算，非新業務規則）。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getCycleLeadTime, type CycleLeadTimeResponse } from '../api/kanbanWidgetsApi';
import { ApiError } from '../api/http';
import type { ItemContentProps } from '../canvas/itemComponentRegistry';
import './WidgetShell.css';

function average(values: number[]): number | null {
  if (values.length === 0) {
    return null;
  }
  return values.reduce((sum, v) => sum + v, 0) / values.length;
}

function formatDays(value: number | null): string {
  return value === null ? '無' : `${value} 天`;
}

function formatAverage(value: number | null): string {
  return value === null ? '—' : `${value.toFixed(1)} 天`;
}

export function CycleLeadTimeDashboardWidget(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const [data, setData] = useState<CycleLeadTimeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    getCycleLeadTime(boardId)
      .then((result) => {
        if (!cancelled) {
          setData(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入 Cycle Time / Lead Time 資料失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId]);

  if (error !== null) {
    return (
      <div className="widget">
        <p role="alert" className="widget__error">
          {error}
        </p>
      </div>
    );
  }

  if (data === null) {
    return (
      <div className="widget">
        <p className="widget__loading">載入中…</p>
      </div>
    );
  }

  const leadTimeAvg = average(data.cards.map((c) => c.leadTimeDays));
  const cycleTimeAvg = average(
    data.cards.map((c) => c.cycleTimeDays).filter((v): v is number => v !== null),
  );

  return (
    <div className="widget">
      <div className="widget__summary">
        <div className="widget__summary-item">
          <span>排除計算的卡片數</span>
          <span>{data.excludedCycleTimeCount}</span>
        </div>
        <div className="widget__summary-item">
          <span>Lead Time 平均值</span>
          <span>{formatAverage(leadTimeAvg)}</span>
        </div>
        <div className="widget__summary-item">
          <span>Lead Time P50/P85/P95</span>
          <span>
            {data.leadTime.p50}／{data.leadTime.p85}／{data.leadTime.p95} 天
          </span>
        </div>
        <div className="widget__summary-item">
          <span>Cycle Time 平均值</span>
          <span>{formatAverage(cycleTimeAvg)}</span>
        </div>
        <div className="widget__summary-item">
          <span>Cycle Time P50/P85/P95</span>
          <span>
            {data.cycleTime.p50}／{data.cycleTime.p85}／{data.cycleTime.p95} 天
          </span>
        </div>
      </div>

      {data.cards.length === 0 ? (
        <p className="widget__empty">尚無已完成卡片</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>卡片標題</th>
              <th>Lead Time</th>
              <th>Cycle Time</th>
              <th>完成時間</th>
            </tr>
          </thead>
          <tbody>
            {data.cards.map((card) => (
              <tr key={card.cardId}>
                <td>{card.title}</td>
                <td>{formatDays(card.leadTimeDays)}</td>
                <td>{formatDays(card.cycleTimeDays)}</td>
                <td>{new Date(card.doneAt).toLocaleDateString('zh-TW')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

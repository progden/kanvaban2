// s-throughput-cfd-dashboard：Throughput 與 CFD 同一個 Screen ID 內以「圖表」欄區分（ui-kanban-widgets.md），
// 不拆兩個元件。CFD 的 Stage 名稱來自 F01 board.stages（跨模組，見 ui-kanban-widgets.md CFD 資料列），
// 這裡另外呼叫既有 getBoard 取得 stage 名稱與顯示順序，widgets API 本身只回傳 stageId。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getBoard, type StageView } from '../api/boardApi';
import {
  getCfd,
  getThroughput,
  type CfdResponse,
  type ThroughputResponse,
  type ThroughputUnit,
} from '../api/kanbanWidgetsApi';
import { ApiError } from '../api/http';
import { useBoardContext } from '../board/BoardContext';
import type { ItemContentProps } from '../canvas/itemComponentRegistry';
import './WidgetShell.css';

export function ThroughputCfdDashboardWidget(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const { cardsVersion } = useBoardContext();
  const [stages, setStages] = useState<StageView[] | null>(null);
  const [unit, setUnit] = useState<ThroughputUnit>('day');
  const [throughput, setThroughput] = useState<ThroughputResponse | null>(null);
  const [cfd, setCfd] = useState<CfdResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    Promise.all([getBoard(boardId), getCfd(boardId)])
      .then(([board, cfdResult]) => {
        if (!cancelled) {
          setStages(board.stages);
          setCfd(cfdResult);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入累積流量圖資料失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, cardsVersion]);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    getThroughput(boardId, unit)
      .then((result) => {
        if (!cancelled) {
          setThroughput(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入 Throughput 資料失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, unit, cardsVersion]);

  if (error !== null) {
    return (
      <div className="widget">
        <p role="alert" className="widget__error">
          {error}
        </p>
      </div>
    );
  }

  if (stages === null || throughput === null || cfd === null) {
    return (
      <div className="widget">
        <p className="widget__loading">載入中…</p>
      </div>
    );
  }

  const stageNameById = new Map(stages.map((s) => [s.id, s.name] as const));
  const orderedStageIds = [...stages].sort((a, b) => a.order - b.order).map((s) => s.id);

  const cfdRows = cfd.dataPoints.flatMap((point) =>
    orderedStageIds
      .filter((stageId) => stageId in point.countByStage)
      .map((stageId) => ({
        date: point.date,
        stageId,
        stageName: stageNameById.get(stageId) ?? stageId,
        count: point.countByStage[stageId],
      })),
  );

  return (
    <div className="widget">
      <div className="widget__section-title">Throughput</div>
      <div className="widget__controls">
        <label className="field-label" htmlFor="throughput-unit">
          單位時間
        </label>
        <select
          id="throughput-unit"
          className="field-input"
          value={unit}
          onChange={(e) => setUnit(e.target.value as ThroughputUnit)}
        >
          <option value="day">日</option>
          <option value="week">週</option>
        </select>
      </div>
      {throughput.periods.length === 0 ? (
        <p className="widget__empty">選定範圍內無完成卡片</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>期間</th>
              <th>完成數</th>
            </tr>
          </thead>
          <tbody>
            {throughput.periods.map((period) => (
              <tr key={period.periodStart}>
                <td>{period.periodStart}</td>
                <td>{period.count}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="widget__section-title">CFD</div>
      {cfdRows.length === 0 ? (
        <p className="widget__empty">尚無卡片資料</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>Stage</th>
              <th>累積卡片數</th>
            </tr>
          </thead>
          <tbody>
            {cfdRows.map((row) => (
              <tr key={`${row.date}-${row.stageId}`}>
                <td>{row.date}</td>
                <td>{row.stageName}</td>
                <td>{row.count}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

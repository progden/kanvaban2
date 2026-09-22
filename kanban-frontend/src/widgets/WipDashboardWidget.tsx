// s-wip-dashboard：依 ui-kanban-widgets.md 操作表，開啟畫面同時觸發 uc-view-wip 與 uc-view-aging-wip。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getAgingWip, getWip, type AgingWipResponse, type WipResponse } from '../api/kanbanWidgetsApi';
import { ApiError } from '../api/http';
import { useBoardContext } from '../board/BoardContext';
import type { ItemContentProps } from '../canvas/itemComponentRegistry';
import './WidgetShell.css';

export function WipDashboardWidget(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const { cardsVersion } = useBoardContext();
  const [wip, setWip] = useState<WipResponse | null>(null);
  const [agingWip, setAgingWip] = useState<AgingWipResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    Promise.all([getWip(boardId), getAgingWip(boardId)])
      .then(([wipResult, agingResult]) => {
        if (!cancelled) {
          setWip(wipResult);
          setAgingWip(agingResult);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入 WIP 資料失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
    // cardsVersion 變動代表卡片被改過（見 board/BoardContext.tsx），放進 dependency array 觸發重抓。
  }, [boardId, cardsVersion]);

  if (error !== null) {
    return (
      <div className="widget">
        <p role="alert" className="widget__error">
          {error}
        </p>
      </div>
    );
  }

  if (wip === null || agingWip === null) {
    return (
      <div className="widget">
        <p className="widget__loading">載入中…</p>
      </div>
    );
  }

  return (
    <div className="widget">
      <div className="widget__section-title">WIP</div>
      {wip.stages.length === 0 ? (
        <p className="widget__empty">尚無卡片</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Stage</th>
              <th>卡片數</th>
            </tr>
          </thead>
          <tbody>
            {wip.stages.map((stage) => (
              <tr key={stage.stageId}>
                <td>{stage.stageName}</td>
                <td>{stage.count}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div className="widget__section-title">Aging WIP</div>
      {agingWip.cards.length === 0 ? (
        <p className="widget__empty">目前沒有進行中的卡片</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>卡片標題</th>
              <th>年齡</th>
            </tr>
          </thead>
          <tbody>
            {agingWip.cards.map((card) => (
              <tr key={card.cardId}>
                <td>{card.title}</td>
                <td>{card.ageDays} 天</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

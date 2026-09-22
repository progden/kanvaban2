// s-duedate-reminder：門檻天數需在 1~365 之間的正整數（uc-view-duedate-reminder pre p1），
// 驗收條件要求「門檻天數輸入為 0 或非正整數時，輸入保留、顯示訊息，且不觸發 uc-view-duedate-reminder」，
// 所以送出前先在前端驗證，不合法就不呼叫 API；錯誤訊息文字與後端 fail-p1 一致。
// 初始門檻天數 spec 未定義固定值（僅定案為使用者查詢時設定，見 spec 變更紀錄），預設 7 天是低風險技術決定。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getDueDateReminder, type DueDateReminderResponse } from '../api/kanbanWidgetsApi';
import { ApiError } from '../api/http';
import type { ItemContentProps } from '../canvas/itemComponentRegistry';
import './WidgetShell.css';

const DEFAULT_THRESHOLD_DAYS = 7;
const INVALID_THRESHOLD_MESSAGE = '門檻天數必須是 1 到 365 之間的正整數';

function parseThreshold(input: string): number | null {
  if (!/^\d+$/.test(input.trim())) {
    return null;
  }
  const value = Number(input.trim());
  if (value < 1 || value > 365) {
    return null;
  }
  return value;
}

export function DueDateReminderWidget(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const [thresholdInput, setThresholdInput] = useState(String(DEFAULT_THRESHOLD_DAYS));
  const [data, setData] = useState<DueDateReminderResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  function query(boardIdValue: string, thresholdDays: number) {
    getDueDateReminder(boardIdValue, thresholdDays)
      .then((result) => setData(result))
      .catch((e: unknown) => setError(e instanceof ApiError ? e.message : '載入截止日期提醒資料失敗，請稍後再試'));
  }

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    query(boardId, DEFAULT_THRESHOLD_DAYS);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [boardId]);

  function handleApply() {
    if (boardId === undefined) {
      return;
    }
    const threshold = parseThreshold(thresholdInput);
    if (threshold === null) {
      setError(INVALID_THRESHOLD_MESSAGE);
      return;
    }
    setError(null);
    query(boardId, threshold);
  }

  return (
    <div className="widget">
      <div className="widget__controls">
        <label className="field-label" htmlFor="duedate-threshold">
          即將到期門檻天數
        </label>
        <input
          id="duedate-threshold"
          className="field-input"
          value={thresholdInput}
          onChange={(e) => setThresholdInput(e.target.value)}
        />
        <button type="button" className="btn-sm" onClick={handleApply}>
          套用
        </button>
      </div>

      {error !== null && (
        <p role="alert" className="widget__error">
          {error}
        </p>
      )}

      {data === null ? (
        <p className="widget__loading">載入中…</p>
      ) : (
        <>
          <div className="widget__section-title">已逾期</div>
          {data.overdue.length === 0 ? (
            <p className="widget__empty">目前沒有已逾期的卡片</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>卡片標題</th>
                  <th>截止日期</th>
                </tr>
              </thead>
              <tbody>
                {data.overdue.map((card) => (
                  <tr key={card.cardId}>
                    <td>{card.title}</td>
                    <td>{card.dueDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <div className="widget__section-title">即將到期</div>
          {data.upcoming.length === 0 ? (
            <p className="widget__empty">目前沒有即將到期的卡片</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>卡片標題</th>
                  <th>截止日期</th>
                </tr>
              </thead>
              <tbody>
                {data.upcoming.map((card) => (
                  <tr key={card.cardId}>
                    <td>{card.title}</td>
                    <td>{card.dueDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}

// s-activity-log：spec-user-membership.md「Feature: 檢視看板活動紀錄」uc-view-board-activity-log
// 對應的畫面內容。ui-user-membership.md 本畫面當時因後端跨 aggregate 投影尚未實作而狀態為
// 「未討論」（見 OQ-29）；T-04 已補上 BoardActivityLogQueryService／GET .../activity-log，
// 這裡依 usecase 區塊 post（「依時間由新到舊列出，每一筆都顯示操作人與動作內容」）與 Gherkin
// Scenario 實作最小可用列表，沒有 ui 檔可循的版面細節（進入方式、欄位呈現）依既有 Canvas item
// 慣例與樣式語彙決定，見交接摘要與對應 OQ。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { viewActivityLog, type ActivityLogEntryView } from '../api/activityLogApi';
import { ApiError } from '../api/http';
import type { ItemContentProps } from './itemComponentRegistry';
import './ActivityLogItem.css';

function formatOccurredAt(occurredAt: string): string {
  return new Date(occurredAt).toLocaleString('zh-TW', { hour12: false });
}

export function ActivityLogItem(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const [entries, setEntries] = useState<ActivityLogEntryView[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    viewActivityLog(boardId)
      .then((result) => {
        if (!cancelled) {
          setEntries(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入活動紀錄失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId]);

  if (error !== null) {
    return (
      <p role="alert" className="activity-log__error">
        {error}
      </p>
    );
  }

  if (entries === null) {
    return <p className="activity-log__loading">載入中…</p>;
  }

  if (entries.length === 0) {
    return <p className="activity-log__empty">尚無活動紀錄</p>;
  }

  return (
    <ul className="activity-log__list" data-testid="activity-log-list">
      {entries.map((entry, index) => (
        <li key={index} className="activity-log__entry">
          <span className="activity-log__operator">
            {entry.operatorDisplayName ?? entry.operatorUsername ?? '未知使用者'}
          </span>
          <span className="activity-log__action">{entry.action}</span>
          <span className="activity-log__time">{formatOccurredAt(entry.occurredAt)}</span>
        </li>
      ))}
    </ul>
  );
}

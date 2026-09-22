// s-board-clock-control：依 ui-board-clock.md 操作表／驗收條件實作。
// 依 OQ-49／spec-canvas-layout.md「待釐清」，這類元件如何被加入畫布的機制尚未定案，
// 這裡只實作 item.component === 'board-clock-control' 的內容本體（見 registerItemComponent 掛載處）。
// 沒有對應設計稿（ui-prototype 只收錄 F01/F02/F07 既有畫面），沿用既有對話框視覺語彙
// （field-label／field-input／btn，見 index.css）做最簡潔可用的版面。
import { useEffect, useState } from 'react';
import { adjustClock, getBoard, pauseClock, resumeClock, type BoardResponse } from '../api/boardApi';
import { listMembers } from '../api/boardMembershipApi';
import { ApiError } from '../api/http';
import { useAuth } from '../auth/useAuth';
import type { ItemContentProps } from './itemComponentRegistry';
import './CanvasStage.css';

export function BoardClockControl({ boardId }: ItemContentProps) {
  const { username } = useAuth();
  const [board, setBoard] = useState<BoardResponse | null>(null);
  const [isOwner, setIsOwner] = useState(false);
  const [targetTime, setTargetTime] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getBoard(boardId), listMembers(boardId)])
      .then(([boardResult, members]) => {
        if (cancelled) {
          return;
        }
        setBoard(boardResult);
        const self = members.find((m) => m.username === username);
        setIsOwner(self !== undefined && self.role === 'OWNER');
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入看板時間失敗，請稍後再試');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, username]);

  async function handleAdjust() {
    if (targetTime === '') {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const updated = await adjustClock(boardId, new Date(targetTime).toISOString());
      setBoard(updated);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '調整看板時間失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  async function handlePauseResume() {
    if (board === null) {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const updated = board.clockStatus === 'PAUSED' ? await resumeClock(boardId) : await pauseClock(boardId);
      setBoard(updated);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '設定看板時間狀態失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="clock-control">載入中…</div>;
  }

  if (board === null) {
    return (
      <div className="clock-control">
        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}
      </div>
    );
  }

  const paused = board.clockStatus === 'PAUSED';

  return (
    <div className="clock-control">
      <div className="clock-control__time" data-testid="clock-control-time">
        {board.clockTime}
      </div>
      <div className="clock-control__status" data-testid="clock-control-status">
        {paused ? 'PAUSED' : 'REALTIME'}
      </div>

      {error !== null && (
        <p role="alert" className="form-error">
          {error}
        </p>
      )}

      {isOwner && (
        <>
          <div className="field">
            <label className="field-label" htmlFor={`clock-control-target-${boardId}`}>
              調整目標時間
            </label>
            <input
              id={`clock-control-target-${boardId}`}
              type="datetime-local"
              step={1}
              className="field-input"
              value={targetTime}
              onChange={(e) => setTargetTime(e.target.value)}
            />
          </div>
          <div className="canvas-dialog__actions">
            <button type="button" className="btn-secondary" disabled={submitting} onClick={() => void handlePauseResume()}>
              {paused ? '恢復' : '暫停'}
            </button>
            <button type="button" className="btn" disabled={submitting || targetTime === ''} onClick={() => void handleAdjust()}>
              調整看板時間
            </button>
          </div>
        </>
      )}
    </div>
  );
}

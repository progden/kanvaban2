// s-canvas：依 ui-canvas-layout.md 操作表／驗收條件實作；版面依 Main.dc.html（見
// .dev/ui-prototype/README.md「檔案與 Screen ID」），只取上方列（返回、看板名稱）與畫布本體的版面，
// 畫布上「看板本體」item 內容由 T-14-fe-board-item 實作，本任務只提供掛載點（見 canvas/itemComponentRegistry.tsx）。
// 「看板成員」item（item.component === 'board-members'，T-15）在此以 side-effect import 註冊，
// 讓使用者透過 CanvasStage 的「＋ 加入元件」放到畫布上時能解析到內容（見 canvas/members/BoardMembersItem.tsx）。
//
// 開啟看板觸發 uc-init-canvas（GET /api/boards/{boardId}/canvas），操作者角色依
// spec-canvas-layout.md 角色定義換算：board-membership.role 為 Owner／Member 對應 r-canvas-editor
// （可編輯），Viewer 對應 r-canvas-viewer（僅能平移縮放與離開，見 ui-canvas-layout.md「資料狀態差異」）。
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getBoard, type BoardResponse } from '../api/boardApi';
import { listMembers } from '../api/boardMembershipApi';
import * as canvasApi from '../api/canvasApi';
import type { CanvasView } from '../api/canvasApi';
import { ApiError } from '../api/http';
import { useAuth } from '../auth/useAuth';
import { BoardContext } from '../board/BoardContext';
import '../board/BoardItemContent';
import { BoardClockControl } from '../canvas/BoardClockControl';
import { CanvasStage } from '../canvas/CanvasStage';
import '../canvas/CanvasStage.css';
import { registerItemComponent } from '../canvas/itemComponentRegistry';
import '../canvas/members/BoardMembersItem';
import { WorkloadDashboard } from '../canvas/WorkloadDashboard';

// item.component === 's-workload-dashboard'（見 ui-workload.md），每個模組各自的 item 內容在這裡
// （畫布掛載處）自行註冊，其餘元件識別碼由各自任務補上；命名為完整 Screen ID，
// 見 ADR-T-17-fe-clock-control-01「修正」段。
registerItemComponent('s-workload-dashboard', WorkloadDashboard, '人員工作量儀表板');
// s-board-clock-control 不是 canvas item，是從下方「看板時間」按鈕開啟的對話框（CR-014）。

export function BoardCanvasPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const { username } = useAuth();
  const [board, setBoard] = useState<BoardResponse | null>(null);
  const [canvas, setCanvas] = useState<CanvasView | null>(null);
  const [canEdit, setCanEdit] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [requestedCardId, setRequestedCardId] = useState<string | null>(null);
  const [cardsVersion, setCardsVersion] = useState(0);
  const [clockDialogOpen, setClockDialogOpen] = useState(false);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    Promise.all([getBoard(boardId), canvasApi.openCanvas(boardId), listMembers(boardId)])
      .then(([boardResult, canvasResult, members]) => {
        if (cancelled) {
          return;
        }
        setBoard(boardResult);
        setCanvas(canvasResult);
        const self = members.find((m) => m.username === username);
        setCanEdit(self !== undefined && self.role !== 'VIEWER');
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入畫布失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, username]);

  if (boardId === undefined) {
    return null;
  }

  return (
    <div className="canvas-page">
      <div className="canvas-page__topbar">
        <button type="button" className="btn-sm" onClick={() => navigate('/boards')}>
          ← 我的看板
        </button>
        {board !== null && <div className="canvas-page__board-name">{board.name}</div>}
        <button type="button" className="btn-sm" onClick={() => setClockDialogOpen(true)}>
          看板時間
        </button>
      </div>

      {clockDialogOpen && boardId !== undefined && (
        <BoardClockControl boardId={boardId} onClose={() => setClockDialogOpen(false)} />
      )}

      {error !== null && (
        <p role="alert" className="form-error canvas-page__error">
          {error}
        </p>
      )}

      {canvas === null && error === null && <p className="canvas-page__error">載入中…</p>}

      {canvas !== null && (
        <BoardContext.Provider
          value={{
            boardId,
            canEdit,
            requestedCardId,
            requestCardDetail: setRequestedCardId,
            clearRequestedCardDetail: () => setRequestedCardId(null),
            cardsVersion,
            notifyCardsChanged: () => setCardsVersion((v) => v + 1),
          }}
        >
          <CanvasStage
            boardId={boardId}
            zoomMin={canvas.zoomMin}
            zoomMax={canvas.zoomMax}
            initialItems={canvas.items}
            initialViewport={canvas.viewport}
            canEdit={canEdit}
          />
        </BoardContext.Provider>
      )}
    </div>
  );
}

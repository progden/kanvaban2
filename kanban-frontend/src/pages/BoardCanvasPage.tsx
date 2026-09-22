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
import { CanvasStage } from '../canvas/CanvasStage';
import '../canvas/CanvasStage.css';
import '../canvas/members/BoardMembersItem';

export function BoardCanvasPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const { username } = useAuth();
  const [board, setBoard] = useState<BoardResponse | null>(null);
  const [canvas, setCanvas] = useState<CanvasView | null>(null);
  const [canEdit, setCanEdit] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
      </div>

      {error !== null && (
        <p role="alert" className="form-error canvas-page__error">
          {error}
        </p>
      )}

      {canvas === null && error === null && <p className="canvas-page__error">載入中…</p>}

      {canvas !== null && (
        <CanvasStage
          boardId={boardId}
          zoomMin={canvas.zoomMin}
          zoomMax={canvas.zoomMax}
          initialItems={canvas.items}
          initialViewport={canvas.viewport}
          canEdit={canEdit}
        />
      )}
    </div>
  );
}

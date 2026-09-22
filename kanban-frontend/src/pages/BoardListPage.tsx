// s-board-list：依 ui-user-membership.md 操作表／驗收條件實作；版面依 BoardList.dc.html
// （見 .dev/ui-prototype/README.md「檔案與 Screen ID」），只調整版面與樣式，行為以 ui／spec 為準。
//
// 「嘗試直接開啟不屬於自己的 Board」（uc-reject-board-access-by-nonmember）不在本畫面實作：
// 依 .dev/loops/implementation-loop/.state/tasks.md，載入單一 Board 內容（s-board）與這個拒絕流程
// 屬於 T-14-fe-board-item 的範圍，T-12 只負責列表本身與建立／刪除對話框。
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listBoards, type BoardResponse } from '../api/boardApi';
import { ApiError } from '../api/http';
import { BoardCreateDialog } from './BoardCreateDialog';
import { BoardDeleteDialog } from './BoardDeleteDialog';
import './BoardListPage.css';

type DialogState = { type: 'create' } | { type: 'delete'; board: BoardResponse } | null;

export function BoardListPage() {
  const navigate = useNavigate();
  const [boards, setBoards] = useState<BoardResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dialog, setDialog] = useState<DialogState>(null);

  useEffect(() => {
    let cancelled = false;
    listBoards()
      .then((result) => {
        if (!cancelled) {
          setBoards(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入 Board 列表失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function handleCreated(board: BoardResponse) {
    setBoards((current) => (current === null ? [board] : [...current, board]));
    setDialog(null);
  }

  function handleDeleted(boardId: string) {
    setBoards((current) => (current === null ? current : current.filter((b) => b.id !== boardId)));
    setDialog(null);
  }

  return (
    <div className="board-list">
      <div className="board-list__header">
        <div>
          <h1>我的看板</h1>
          <p className="board-list__subtitle">你是 Owner 或 Member 的看板都在這裡。</p>
        </div>
        {boards !== null && boards.length > 0 && (
          <button type="button" className="btn" onClick={() => setDialog({ type: 'create' })}>
            建立看板
          </button>
        )}
      </div>

      {error !== null && (
        <p role="alert" className="form-error">
          {error}
        </p>
      )}

      {boards === null && error === null && <p>載入中…</p>}

      {boards !== null && boards.length === 0 && (
        <div className="board-list__empty">
          <p>
            你還沒有看板。
            <br />
            建一個，開始把工作攤在畫布上。
          </p>
          <button type="button" className="btn" onClick={() => setDialog({ type: 'create' })}>
            建立看板
          </button>
        </div>
      )}

      {boards !== null && boards.length > 0 && (
        <div className="board-list__grid">
          {boards.map((board) => (
            <div className="board-list-card" key={board.id}>
              <button
                type="button"
                className="board-list-card__open"
                onClick={() => navigate(`/boards/${board.id}`)}
              >
                {board.name}
              </button>
              <button
                type="button"
                className="btn-sm board-list-card__delete"
                onClick={() => setDialog({ type: 'delete', board })}
              >
                刪除
              </button>
            </div>
          ))}
        </div>
      )}

      {dialog?.type === 'create' && (
        <BoardCreateDialog onCancel={() => setDialog(null)} onCreated={handleCreated} />
      )}
      {dialog?.type === 'delete' && (
        <BoardDeleteDialog board={dialog.board} onCancel={() => setDialog(null)} onDeleted={handleDeleted} />
      )}
    </div>
  );
}

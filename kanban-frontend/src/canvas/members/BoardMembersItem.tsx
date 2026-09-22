// 「看板成員」item：ui-canvas-layout.md s-canvas 操作表「於『看板成員』item 選擇加入成員」的掛載內容
// （進入點見 OQ-45／spec-canvas-layout.md 待釐清，機制細節由本任務決定，見 decision-log）。
// item.component 值 "board-members" 由本任務選定（spec「待釐清」未定案，屬技術實作細節，不影響行為）。
// 本 item 顯示目前成員的頭像清單，點擊任一處開啟 s-member-management（MemberManagementDialog）。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import * as membershipApi from '../../api/boardMembershipApi';
import type { MemberView } from '../../api/boardMembershipApi';
import { ApiError } from '../../api/http';
import { useAuth } from '../../auth/useAuth';
import type { ItemContentProps } from '../itemComponentRegistry';
import { registerItemComponent } from '../itemComponentRegistry';
import { MemberManagementDialog } from './MemberManagementDialog';
import './BoardMembersItem.css';

export function BoardMembersItem(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const { username } = useAuth();
  const [members, setMembers] = useState<MemberView[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    membershipApi
      .listMembers(boardId)
      .then((result) => {
        if (!cancelled) {
          setMembers(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入看板成員失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, dialogOpen]);

  if (boardId === undefined) {
    return null;
  }

  return (
    <div className="board-members-item">
      <button
        type="button"
        className="board-members-item__trigger"
        onClick={() => setDialogOpen(true)}
        aria-label="看板成員，選擇加入成員"
      >
        {error !== null && <span className="board-members-item__error">{error}</span>}
        {error === null && members === null && <span>載入中…</span>}
        {error === null && members !== null && (
          <div className="board-members-item__avatars">
            {members.map((member) => (
              <span key={member.username} className="avatar board-members-item__avatar" aria-hidden="true">
                {member.displayName.charAt(0)}
              </span>
            ))}
          </div>
        )}
      </button>

      {dialogOpen && username !== null && (
        <MemberManagementDialog boardId={boardId} currentUsername={username} onClose={() => setDialogOpen(false)} />
      )}
    </div>
  );
}

registerItemComponent('board-members', BoardMembersItem);

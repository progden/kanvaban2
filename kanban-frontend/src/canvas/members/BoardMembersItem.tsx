// 「看板成員」item：ui-canvas-layout.md s-canvas 操作表「於『看板成員』item 選擇加入成員」的掛載內容
// （進入點見 OQ-45／spec-canvas-layout.md 待釐清，機制細節由本任務決定，見 decision-log）。
// item.component 值 "board-members" 由本任務選定（spec「待釐清」未定案，屬技術實作細節，不影響行為）。
// 本 item 顯示目前成員的頭像清單，點擊任一處開啟 s-member-management（MemberManagementDialog）。
// 頭像本身也是 uc-assign-card-owner-by-drag（spec-user-membership.md）的拖曳來源之一：規格只寫
// 「拖曳成員的頭像」，沒有限定是哪個畫面的頭像；原本只有「看板工作量」item（WorkloadDashboard）
// 接了這個協定，是 T-19-fe-workload 當時的實作範圍，不是刻意排除這裡——補上同一套協定
// （cardAssigneeDrag.ts），拖曳本身是 HTML5 原生手勢，跟外層 button 的 click 不衝突（成功拖曳
// 後瀏覽器不會再對同一次操作觸發 click）。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import * as membershipApi from '../../api/boardMembershipApi';
import type { MemberView } from '../../api/boardMembershipApi';
import { ApiError } from '../../api/http';
import { useAuth } from '../../auth/useAuth';
import { startUserAvatarDrag } from '../cardAssigneeDrag';
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
  // r-board-viewer 不能寫入任何內容（spec-user-membership.md 角色定義），拖曳追加負責人是寫入動作，
  // 比照 WorkloadDashboard 的角色判斷方式；members 還沒載入完成時預設不可拖曳。
  const self = members?.find((m) => m.username === username);
  const canDrag = self !== undefined && self.role !== 'VIEWER';

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
              <span
                key={member.username}
                className="avatar board-members-item__avatar"
                draggable={canDrag}
                data-testid={`board-members-avatar-${member.userId}`}
                onDragStart={(e) => startUserAvatarDrag(e, member.userId)}
                // 拖曳中的 mousedown/dragstart 不該連帶觸發外層按鈕的「開啟成員管理」，
                // 但單純點擊（沒有拖曳）仍要讓它 bubble 上去開對話框，所以這裡不呼叫 stopPropagation。
                title={canDrag ? `${member.displayName}：拖曳到卡片上追加為負責人` : member.displayName}
              >
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

registerItemComponent('board-members', BoardMembersItem, '看板成員');

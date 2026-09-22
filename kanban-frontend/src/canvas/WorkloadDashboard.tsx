// s-workload-dashboard：依 ui-workload.md 操作表／驗收條件實作。
// 沒有對應設計稿（ui-prototype 只收錄 F01/F02/F07 既有畫面），沿用既有儀表板／清單視覺語彙做最簡潔可用的版面。
// 拖曳成員頭像到卡片：拖放目標卡片縮圖屬於 T-14-fe-board-item（本任務動工時尚未合併），
// 拖放協定另抽成 cardAssigneeDrag.ts 共用模組，T-14 只需在卡片節點接上 handleCardAssigneeDrop。
import { useEffect, useState } from 'react';
import { listMembers } from '../api/boardMembershipApi';
import { ApiError } from '../api/http';
import { getWorkload, type MemberWorkloadEntry } from '../api/workloadApi';
import { useAuth } from '../auth/useAuth';
import { startUserAvatarDrag } from './cardAssigneeDrag';
import { CardsByAssigneeDialog } from './CardsByAssigneeDialog';
import type { ItemContentProps } from './itemComponentRegistry';
import './CanvasStage.css';

export function WorkloadDashboard({ boardId }: ItemContentProps) {
  const { username } = useAuth();
  const [members, setMembers] = useState<MemberWorkloadEntry[] | null>(null);
  const [unassignedCount, setUnassignedCount] = useState(0);
  const [canDrag, setCanDrag] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<MemberWorkloadEntry | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getWorkload(boardId), listMembers(boardId)])
      .then(([workload, boardMembers]) => {
        if (cancelled) {
          return;
        }
        setMembers(workload.members);
        setUnassignedCount(workload.unassignedCount);
        // r-board-viewer「不能新增／編輯／移動／刪除任何內容」（spec-user-membership.md 角色定義），
        // 拖曳追加負責人是寫入動作，Viewer 不可操作，比照 BoardClockControl 的角色判斷方式。
        const self = boardMembers.find((m) => m.username === username);
        setCanDrag(self !== undefined && self.role !== 'VIEWER');
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入工作量表失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, username]);

  if (error !== null) {
    return (
      <div className="workload-dashboard">
        <p role="alert" className="form-error">
          {error}
        </p>
      </div>
    );
  }

  if (members === null) {
    return <div className="workload-dashboard">載入中…</div>;
  }

  return (
    <div className="workload-dashboard">
      <ul data-testid="workload-member-list" className="workload-dashboard__list">
        {members.map((member) => (
          <li key={member.userId} className="workload-dashboard__row">
            <span
              className="workload-dashboard__avatar"
              draggable={canDrag}
              data-testid={`workload-avatar-${member.userId}`}
              onDragStart={(e) => startUserAvatarDrag(e, member.userId)}
              title={canDrag ? '拖曳到卡片上追加為負責人' : member.displayName}
            >
              {member.displayName.slice(0, 1)}
            </span>
            <span className="workload-dashboard__name">{member.displayName}</span>
            <button
              type="button"
              className="workload-dashboard__count"
              data-testid={`workload-count-${member.userId}`}
              onClick={() => setSelected(member)}
            >
              {member.cardCount}
            </button>
          </li>
        ))}
      </ul>

      <div className="workload-dashboard__unassigned">
        未指派：<span data-testid="workload-unassigned-count">{unassignedCount}</span>
      </div>

      {selected !== null && (
        <CardsByAssigneeDialog
          boardId={boardId}
          userId={selected.userId}
          displayName={selected.displayName}
          onClose={() => setSelected(null)}
        />
      )}
    </div>
  );
}

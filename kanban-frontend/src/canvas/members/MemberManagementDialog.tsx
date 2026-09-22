// s-member-management：依 ui-user-membership.md 操作表／驗收條件實作；版面依 MemberManagement.dc.html
// （見 .dev/ui-prototype/README.md「檔案與 Screen ID」），對話框沿用 index.css 的 .dialog-backdrop／
// .dialog-panel／.field／.btn 系列，成員清單樣式另見 MemberManagementDialog.css。
//
// 「移除成員」需確認的時機只在目標成員仍是卡片負責人時（uc-remove-member pre p2、驗收條件「移除仍是卡片
// 負責人的成員時，先顯示確認訊息並告知卡片張數，確認後才觸發 uc-remove-member」；驗收條件沒有為一般移除
// 另外要求確認步驟）：先以 confirmed=false 呼叫，後端在這種情況下回 409、訊息內含卡片張數，前端據此顯示
// 確認卡片，確認後才以 confirmed=true 重打；訊息不符合這個樣式（例如「看板至少需要保留一位 Owner」）時，
// 視為一般失敗，直接顯示訊息，不進入確認流程。
// 「變更成員角色」則不論卡片狀態一律先確認（ui-user-membership.md 該列「需確認？」：spec 未定義將 Owner
// 降級為 Member 的操作，視為不可逆）。
import { useEffect, useState } from 'react';
import * as membershipApi from '../../api/boardMembershipApi';
import type { BoardRole, MemberView } from '../../api/boardMembershipApi';
import { ApiError } from '../../api/http';
import './MemberManagementDialog.css';

const CARD_ASSIGNEE_CONFIRMATION_PATTERN = /仍是\s*\d+\s*張卡片的負責人/;

interface MemberManagementDialogProps {
  boardId: string;
  currentUsername: string;
  onClose: () => void;
}

function displayRole(role: BoardRole): string {
  return role.charAt(0) + role.slice(1).toLowerCase();
}

export function MemberManagementDialog({ boardId, currentUsername, onClose }: MemberManagementDialogProps) {
  const [members, setMembers] = useState<MemberView[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [inviteUsername, setInviteUsername] = useState('');
  const [inviteRole, setInviteRole] = useState<BoardRole>('MEMBER');
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [inviting, setInviting] = useState(false);

  const [roleChangeTarget, setRoleChangeTarget] = useState<string | null>(null);
  const [roleChangeError, setRoleChangeError] = useState<string | null>(null);
  const [roleChanging, setRoleChanging] = useState(false);

  const [removeTarget, setRemoveTarget] = useState<{ username: string; message: string } | null>(null);
  const [removeError, setRemoveError] = useState<{ username: string; message: string } | null>(null);
  const [removing, setRemoving] = useState(false);

  function refreshMembers() {
    return membershipApi.listMembers(boardId).then(setMembers);
  }

  useEffect(() => {
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
          setLoadError(e instanceof ApiError ? e.message : '載入成員清單失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId]);

  const self = members?.find((m) => m.username === currentUsername) ?? null;
  const isOwner = self?.role === 'OWNER';

  async function handleInvite() {
    setInviting(true);
    setInviteError(null);
    try {
      await membershipApi.inviteMember(boardId, inviteUsername, inviteRole);
      setInviteUsername('');
      await refreshMembers();
    } catch (e) {
      setInviteError(e instanceof ApiError ? e.message : '邀請成員失敗，請稍後再試');
    } finally {
      setInviting(false);
    }
  }

  async function handleConfirmRoleChange() {
    if (roleChangeTarget === null) {
      return;
    }
    setRoleChanging(true);
    setRoleChangeError(null);
    try {
      await membershipApi.changeMemberRole(boardId, roleChangeTarget, 'OWNER');
      setRoleChangeTarget(null);
      await refreshMembers();
    } catch (e) {
      setRoleChangeError(e instanceof ApiError ? e.message : '變更成員角色失敗，請稍後再試');
    } finally {
      setRoleChanging(false);
    }
  }

  async function handleRemoveClick(username: string) {
    setRemoveError(null);
    setRemoving(true);
    try {
      await membershipApi.removeMember(boardId, username, false);
      await refreshMembers();
    } catch (e) {
      if (e instanceof ApiError && CARD_ASSIGNEE_CONFIRMATION_PATTERN.test(e.message)) {
        setRemoveTarget({ username, message: e.message });
      } else {
        setRemoveError({ username, message: e instanceof ApiError ? e.message : '移除成員失敗，請稍後再試' });
      }
    } finally {
      setRemoving(false);
    }
  }

  async function handleConfirmRemove() {
    if (removeTarget === null) {
      return;
    }
    const { username } = removeTarget;
    setRemoving(true);
    setRemoveError(null);
    try {
      await membershipApi.removeMember(boardId, username, true);
      setRemoveTarget(null);
      await refreshMembers();
    } catch (e) {
      setRemoveTarget(null);
      setRemoveError({ username, message: e instanceof ApiError ? e.message : '移除成員失敗，請稍後再試' });
    } finally {
      setRemoving(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel member-management-dialog">
        <div className="member-management-dialog__header">
          <div>
            <h2>看板成員</h2>
            {members !== null && <p className="member-management-dialog__count">{members.length} 人</p>}
          </div>
          <button type="button" className="btn-sm" aria-label="關閉" onClick={onClose}>
            關閉
          </button>
        </div>

        {loadError !== null && (
          <p role="alert" className="form-error">
            {loadError}
          </p>
        )}

        <div className="member-management-dialog__invite">
          <label className="field-label" htmlFor="member-management-invite-username">
            邀請成員
          </label>
          <div className="member-management-dialog__invite-row">
            <input
              id="member-management-invite-username"
              className="field-input"
              value={inviteUsername}
              onChange={(e) => setInviteUsername(e.target.value)}
            />
            <label htmlFor="member-management-invite-role" className="member-management-dialog__visually-hidden">
              邀請角色
            </label>
            <select
              id="member-management-invite-role"
              className="field-input member-management-dialog__role-select"
              value={inviteRole}
              onChange={(e) => setInviteRole(e.target.value as BoardRole)}
            >
              <option value="MEMBER">Member</option>
              <option value="OWNER">Owner</option>
            </select>
            <button
              type="button"
              className="btn"
              disabled={inviting || inviteUsername === ''}
              onClick={() => void handleInvite()}
            >
              邀請
            </button>
          </div>
          {inviteError !== null && (
            <p role="alert" className="form-error member-management-dialog__invite-error">
              {inviteError}
            </p>
          )}
        </div>

        {members === null && loadError === null && <p>載入中…</p>}

        {members !== null && (
          <ul className="member-management-dialog__list">
            {members.map((member) => (
              <li key={member.username} className="member-management-dialog__row">
                <div className="member-management-dialog__row-main">
                  <span className="avatar" aria-hidden="true">
                    {member.displayName.charAt(0)}
                  </span>
                  <div className="member-management-dialog__identity">
                    <div className="member-management-dialog__display-name">{member.displayName}</div>
                    <div className="member-management-dialog__username">{member.username}</div>
                  </div>
                  <span className={`member-management-dialog__role-badge member-management-dialog__role-badge--${member.role.toLowerCase()}`}>
                    {displayRole(member.role)}
                  </span>

                  {member.role !== 'OWNER' && (
                    <button
                      type="button"
                      className="btn-sm"
                      onClick={() => {
                        setRoleChangeError(null);
                        setRoleChangeTarget(member.username);
                      }}
                    >
                      設為 Owner
                    </button>
                  )}
                  {isOwner && (
                    <button
                      type="button"
                      className="btn-sm member-management-dialog__remove"
                      disabled={removing}
                      onClick={() => void handleRemoveClick(member.username)}
                    >
                      移除
                    </button>
                  )}
                </div>

                {roleChangeTarget === member.username && (
                  <div className="member-management-dialog__confirm">
                    <p>把 {member.displayName} 設為 Owner？這個動作沒辦法改回來。</p>
                    {roleChangeError !== null && (
                      <p role="alert" className="form-error">
                        {roleChangeError}
                      </p>
                    )}
                    <div className="member-management-dialog__confirm-actions">
                      <button
                        type="button"
                        className="btn-sm"
                        onClick={() => {
                          setRoleChangeTarget(null);
                          setRoleChangeError(null);
                        }}
                      >
                        取消
                      </button>
                      <button
                        type="button"
                        className="btn-sm"
                        disabled={roleChanging}
                        onClick={() => void handleConfirmRoleChange()}
                      >
                        設為 Owner
                      </button>
                    </div>
                  </div>
                )}

                {removeTarget !== null && removeTarget.username === member.username && (
                  <div className="member-management-dialog__confirm">
                    <p>{removeTarget.message}</p>
                    <div className="member-management-dialog__confirm-actions">
                      <button type="button" className="btn-sm" onClick={() => setRemoveTarget(null)}>
                        取消
                      </button>
                      <button
                        type="button"
                        className="btn-sm member-management-dialog__remove"
                        disabled={removing}
                        onClick={() => void handleConfirmRemove()}
                      >
                        移除
                      </button>
                    </div>
                  </div>
                )}

                {removeError !== null && removeError.username === member.username && (
                  <p role="alert" className="form-error">
                    {removeError.message}
                  </p>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

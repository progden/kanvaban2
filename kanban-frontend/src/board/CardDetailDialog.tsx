// s-card-detail：依 ui-kanban-basic.md 操作表／驗收條件實作；版面依 CardDetail.dc.html
// （見 .dev/ui-prototype/README.md）。uc-edit-card 只更新 description／due-date／labels，標題不可編輯；
// 負責人透過 s-card-assignee-picker（F02）調整，本畫面只顯示、純前端導覽不觸發 Use Case。
import { useEffect, useState } from 'react';
import {
  addComment,
  editCard,
  getCard,
  listAssigneeCandidates,
  type AssigneeCandidate,
  type CardResponse,
} from '../api/cardApi';
import { ApiError } from '../api/http';
import { avatarColorFor } from './avatarColor';
import { CardAssigneePicker } from './CardAssigneePicker';
import './Board.css';

interface CardDetailDialogProps {
  boardId: string;
  cardId: string;
  swimlaneName: string;
  stageName: string;
  onClose: () => void;
  onChanged: () => void;
}

export function CardDetailDialog({
  boardId,
  cardId,
  swimlaneName,
  stageName,
  onClose,
  onChanged,
}: CardDetailDialogProps) {
  const [card, setCard] = useState<CardResponse | null>(null);
  const [candidates, setCandidates] = useState<AssigneeCandidate[]>([]);
  const [description, setDescription] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [labelsText, setLabelsText] = useState('');
  const [commentContent, setCommentContent] = useState('');
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [commentError, setCommentError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [pickerOpen, setPickerOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getCard(cardId), listAssigneeCandidates(boardId)])
      .then(([loadedCard, loadedCandidates]) => {
        if (cancelled) {
          return;
        }
        setCard(loadedCard);
        setCandidates(loadedCandidates);
        setDescription(loadedCard.description ?? '');
        setDueDate(loadedCard.dueDate ?? '');
        setLabelsText(loadedCard.labels.join('、'));
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setLoadError(e instanceof ApiError ? e.message : '載入卡片內容失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, cardId]);

  async function handleSave() {
    setSaving(true);
    try {
      const labels = labelsText
        .split('、')
        .map((label) => label.trim())
        .filter((label) => label.length > 0);
      const updated = await editCard(cardId, description, dueDate.length === 0 ? null : dueDate, labels);
      setCard(updated);
      onChanged();
    } catch (e) {
      setSaveError(e instanceof ApiError ? e.message : '儲存變更失敗，請稍後再試');
    } finally {
      setSaving(false);
    }
  }

  async function handleAddComment() {
    try {
      await addComment(cardId, commentContent);
      const updated = await getCard(cardId);
      setCard(updated);
      setCommentContent('');
    } catch (e) {
      setCommentError(e instanceof ApiError ? e.message : '新增留言失敗，請稍後再試');
    }
  }

  function displayNameOf(userId: string): string {
    return candidates.find((c) => c.id === userId)?.displayName ?? userId;
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel board-card-detail">
        {loadError !== null && (
          <p role="alert" className="form-error">
            {loadError}
          </p>
        )}

        {card === null && loadError === null && <p>載入中…</p>}

        {card !== null && (
          <>
            <div className="board-card-detail__head">
              <div>
                <div className="board-card-detail__breadcrumb">
                  {swimlaneName} / {stageName}
                </div>
                <h1 className="board-card-detail__title">{card.title}</h1>
              </div>
              <button type="button" className="btn-sm" aria-label="關閉卡片詳情" onClick={onClose}>
                關閉
              </button>
            </div>

            <div className="board-card-detail__body">
              <div className="board-card-detail__main">
                <label className="field-label" htmlFor="card-detail-description">
                  描述
                </label>
                <textarea
                  id="card-detail-description"
                  className="board-card-detail__textarea"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />

                <h2 className="board-card-detail__section-title">留言</h2>
                <ul className="board-card-detail__comments">
                  {card.comments.map((comment) => (
                    <li key={comment.id}>
                      <div className="board-card-detail__comment-author">{displayNameOf(comment.authorId)}</div>
                      <p>{comment.content}</p>
                    </li>
                  ))}
                </ul>

                {commentError !== null && (
                  <p role="alert" className="form-error">
                    {commentError}
                  </p>
                )}
                <label className="field-label" htmlFor="card-detail-comment">
                  新增留言
                </label>
                <div className="board-card-detail__comment-form">
                  <textarea
                    id="card-detail-comment"
                    value={commentContent}
                    onChange={(event) => setCommentContent(event.target.value)}
                  />
                  <button type="button" className="btn" onClick={() => void handleAddComment()}>
                    送出
                  </button>
                </div>
              </div>

              <div className="board-card-detail__side">
                <label className="field-label" htmlFor="card-detail-due">
                  截止日期
                </label>
                <input
                  id="card-detail-due"
                  className="field-input"
                  type="date"
                  value={dueDate}
                  onChange={(event) => setDueDate(event.target.value)}
                />

                <label className="field-label" htmlFor="card-detail-labels">
                  標籤（用、分隔）
                </label>
                <input
                  id="card-detail-labels"
                  className="field-input"
                  type="text"
                  value={labelsText}
                  onChange={(event) => setLabelsText(event.target.value)}
                />

                <span className="field-label">負責人</span>
                <div className="board-card-detail__assignees">
                  {card.assigneeIds.length === 0 && <span>未指派</span>}
                  {card.assigneeIds.map((userId) => (
                    <span
                      key={userId}
                      className="avatar"
                      style={{ background: avatarColorFor(userId) }}
                      title={displayNameOf(userId)}
                    >
                      {displayNameOf(userId).charAt(0)}
                    </span>
                  ))}
                  <button type="button" className="btn-sm" onClick={() => setPickerOpen(true)}>
                    變更
                  </button>
                </div>

                {saveError !== null && (
                  <p role="alert" className="form-error">
                    {saveError}
                  </p>
                )}
                <button type="button" className="btn" onClick={() => void handleSave()} disabled={saving}>
                  儲存變更
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {pickerOpen && card !== null && (
        <CardAssigneePicker
          boardId={boardId}
          card={card}
          onCancel={() => setPickerOpen(false)}
          onSaved={(updated) => {
            setCard(updated);
            setPickerOpen(false);
            onChanged();
          }}
        />
      )}
    </div>
  );
}

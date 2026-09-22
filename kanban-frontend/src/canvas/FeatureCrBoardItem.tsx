// s-feature-cr-board：spec-feature-cr-board.md「Feature: Feature／CR 追蹤表」uc-view-feature-cr-board
// 對應的畫面內容。ui-feature-cr-board.md「進入與離開」註明本畫面以 F07 item 形式顯示於 s-canvas，
// 「如何新增此類元件的具體機制仍待該 spec『待釐清』與整合 CR 定案（依 OQ-49）」——比照 T-16
// （s-activity-log）已採用的既有結論：沿用 T-13 一般化的「＋ 加入元件」機制掛載，不另開專屬入口。
// item.component 字串值（"feature-cr-board"）與版面（清單樣式、orphan／警告區塊呈現）為本任務依
// ui-feature-cr-board.md「資料」「狀態」欄與既有 Canvas item 樣式語彙推論決定，已開 OQ 說明。
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { viewFeatureCrBoard, type FeatureView } from '../api/featureCrBoardApi';
import { ApiError } from '../api/http';
import { useBoardContext } from '../board/BoardContext';
import type { ItemContentProps } from './itemComponentRegistry';
import './FeatureCrBoardItem.css';

export function FeatureCrBoardItem(_props: ItemContentProps) {
  const { boardId } = useParams<{ boardId: string }>();
  const { cardsVersion } = useBoardContext();
  const [view, setView] = useState<{ features: FeatureView[]; orphanCrIds: string[]; warnings: string[] } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (boardId === undefined) {
      return;
    }
    let cancelled = false;
    viewFeatureCrBoard(boardId)
      .then((result) => {
        if (!cancelled) {
          setView(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入 Feature／CR 追蹤表失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, cardsVersion]);

  if (error !== null) {
    return (
      <p role="alert" className="form-error feature-cr-board__error">
        {error}
      </p>
    );
  }

  if (view === null) {
    return <p className="feature-cr-board__loading">載入中…</p>;
  }

  const isEmpty = view.features.length === 0 && view.orphanCrIds.length === 0;

  return (
    <div className="feature-cr-board" data-testid="feature-cr-board">
      {view.warnings.length > 0 && (
        <ul className="feature-cr-board__warnings" data-testid="feature-cr-board-warnings">
          {view.warnings.map((warning, index) => (
            <li key={index} className="feature-cr-board__warning">
              {warning}
            </li>
          ))}
        </ul>
      )}

      {isEmpty ? (
        <p className="feature-cr-board__empty">尚無 Feature／CR 卡片</p>
      ) : (
        <ul className="feature-cr-board__feature-list" data-testid="feature-cr-board-features">
          {view.features.map((feature) => (
            <li key={feature.featureId} className="feature-cr-board__feature">
              <div className="feature-cr-board__feature-header">
                <span className="feature-cr-board__feature-id">{feature.featureId}</span>
                <span className="feature-cr-board__status">{feature.status}</span>
              </div>
              {feature.crs.length > 0 && (
                <ul className="feature-cr-board__cr-list">
                  {feature.crs.map((cr) => (
                    <li key={cr.crId} className="feature-cr-board__cr">
                      <span className="feature-cr-board__cr-id">{cr.crId}</span>
                      <span className="feature-cr-board__status">{cr.status}</span>
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      )}

      {view.orphanCrIds.length > 0 && (
        <div className="feature-cr-board__orphans">
          <span className="feature-cr-board__orphans-title">orphan CR</span>
          <ul className="feature-cr-board__orphan-list" data-testid="feature-cr-board-orphans">
            {view.orphanCrIds.map((crId) => (
              <li key={crId} className="feature-cr-board__orphan">
                {crId}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

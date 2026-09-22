// 對應 ui-user-membership.md s-cards-by-assignee 操作表／驗收條件／狀態段。
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CardsByAssigneeDialog } from './CardsByAssigneeDialog';

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('s-cards-by-assignee', () => {
  it('查詢對象同時是多張卡片的負責人時，清單同時包含這些卡片', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse([
        { id: 'card-a', title: '卡片 A' },
        { id: 'card-b', title: '卡片 B' },
      ]),
    );

    render(<CardsByAssigneeDialog boardId="board-a" userId="u1" displayName="雅婷" onClose={() => {}} />);

    await waitFor(() => expect(screen.getByTestId('cards-by-assignee-list')).toHaveTextContent('卡片 A'));
    expect(screen.getByTestId('cards-by-assignee-list')).toHaveTextContent('卡片 B');
  });

  it('查詢對象目前沒有負責任何卡片時，清單顯示為空', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse([]));

    render(<CardsByAssigneeDialog boardId="board-a" userId="u1" displayName="雅婷" onClose={() => {}} />);

    await waitFor(() => expect(screen.getByText('目前沒有負責任何卡片')).toBeInTheDocument());
  });

  it('點擊關閉按鈕觸發 onClose，回到 s-workload-dashboard', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse([]));
    const onClose = vi.fn();

    render(<CardsByAssigneeDialog boardId="board-a" userId="u1" displayName="雅婷" onClose={onClose} />);
    await waitFor(() => expect(screen.getByText('目前沒有負責任何卡片')).toBeInTheDocument());

    fireEvent.click(screen.getByText('關閉'));

    expect(onClose).toHaveBeenCalled();
  });
});

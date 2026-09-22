# T-15-fe-member-management state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪待審。本輪只修 D-01、D-02，未動其他範圍。

- D-01：「移除成員」改成一律先顯示確認卡才打 API；卡片負責人情境維持先 confirmed=false 再依 409 張數訊息二次確認、confirmed=true 重打；其他失敗維持關閉確認卡＋列下方顯示訊息。三個既有移除相關測試同步改斷言與標題。
- D-02：「設為 Owner」顯示條件改成 `member.role === 'MEMBER'`（原 `!== 'OWNER'` 會誤放行 Viewer），新增 Viewer 無此按鈕的測試。

Check：`pnpm test` 8 檔 55 測試全過、`pnpm run build` 通過、`pnpm run lint` 僅剩 T-13 既有 1 則 warning（本輪未動的檔案）。

Review 請先看：`MemberManagementDialog.tsx` 的 `handleRemoveClick`／`handleConfirmRemove` 新流程是否確實符合「移除成員」「需確認？＝是」且卡片負責人二次確認保留；`member.role === 'MEMBER'` 條件是否確實擋掉 Viewer。

OQ-01／OQ-02(2)／OQ-03 維持不阻塞，本輪無新開 OQ。

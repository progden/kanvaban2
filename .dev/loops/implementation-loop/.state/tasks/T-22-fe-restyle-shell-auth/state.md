# T-22-fe-restyle-shell-auth state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪待審。D-01～D-04 本輪全部修好：

- D-01：Logo 3x3 網格淺色格改為第 3、4、8 格（三處共用同一 class，一次修好）。
- D-02：補上 `--color-text-label: #3b4756`，修正 `.field-label`／`.btn-secondary`／`.btn-sm`／`.auth-brand__note-title` 誤用色。
- D-03：`.auth-brand`／`.auth-form` 尺寸、auth 品牌欄 Logo／標語字級對齊設計稿，s-login 補回看板示意插圖。
- D-04：`s-signup` 移除沒有 spec 依據的「之後不能改」宣稱；其餘設計稿標「規格未定義」的品牌文案（產品名稱占位字、標語、插圖、說明卡文案）開立 OQ-T-22-fe-restyle-shell-auth-01（高、不阻塞、owner 人工）。

Review 請先看：`decision-log.md` 這輪紀錄逐項對照設計稿數值；`git diff` 只有 4 個檔案；`pnpm test`／`lint`／`build` 都是本輪重跑過的結果，都綠。

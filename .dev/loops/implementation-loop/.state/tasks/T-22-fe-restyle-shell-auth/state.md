# T-22-fe-restyle-shell-auth state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪：退回（doing）。
- pnpm test 16/16 綠燈，lint 與 build 都通過，任務邊界乾淨，行為沒有變動。
- D-01：Logo 3x3 淺色格位置錯，設計稿是第 3、4、8 格。
- D-02：共用色票少了 #3B4756，標籤、次要按鈕、小按鈕誤用 #5C6878。
- D-03：auth 頁的品牌欄寬、padding、Logo 尺寸、標語字級、登入頁插圖跟設計稿不一致，也沒有記下理由。
- D-04：[產品名稱]、標語、註冊說明卡都是設計稿標為「規格未定義」的文案，Dev 沒開 OQ；『之後不能改』沒有 spec 依據。

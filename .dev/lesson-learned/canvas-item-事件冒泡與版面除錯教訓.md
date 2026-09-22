# F07 畫布（canvas item）UI 除錯教訓

> 來源：2026-09-22～23 一次對話 session，從「跑版」「元件識別碼應該是下拉選單」「workload 沒同步」
> 「新增卡片點不進輸入框」一路查到底的過程；`kanban-frontend` 尚未有前端 e2e／截圖比對的先例，
> 這份順便記錄「怎麼確認前端真的修好」的方法，不只記 bug 本身。
> 每則格式：現象（附證據）→ 根因 → 規則。

## 1. 「跑版」不要先猜是哪個元件的問題，先跟 `.dev/ui-prototype/` 截圖比對

- **現象**：user 回報「跑版」，第一輪直接動手把畫布左上角的「＋ 加入元件」按鈕移到右上角、把 item 預設座標從 (0,0) 改成 (0,56) 來避開重疊。user 追問「跑截圖比對 prototype」後才發現：`Main.dc.html` 原型裡按鈕本來就在左上角，item 也是從 (0,0) 開始——真正的問題不是「誰的位置錯了」，而是沒有替 item 留出視覺淨空。
- **根因**：沒有先建立「應該長什麼樣子」的基準（原型截圖）就開始動位置，等於憑印象修 CSS，容易把「規格定死的值」跟「單純沒協調好的視覺細節」搞混，也容易把座標改到看起來不重疊但跟設計初衷不符的地方。
- **規則**：
  1. 收到「跑版」回報，先用 Playwright 把 `.dev/ui-prototype/*.dc.html` 對應畫面渲染成截圖，跟目前實作的截圖並排看，再動手。
  2. 修位置前先分清楚「這個值是不是 spec 定死的」：查 usecase 區塊的 `post`（例：`spec-canvas-layout.md` 第 100 行 `uc-init-canvas` 明寫 `item.x` 為 0、`item.y` 為 0）。定死的值不能動，要在**沒有定義的那一端**（這次是「使用者第一次自己平移前，前端本地要用什麼初始 viewport」——`viewport` 這個 row 在使用者操作前根本不存在）找空間。
  3. 判斷改動要不要走 CR，看檔頭「狀態」：`草稿`／`討論中` 可以直接改（這次 `spec-canvas-layout.md` 是草稿、`ui-canvas-layout.md` 是討論中）；`定稿` 才需要 CR。改之前先 `grep "^狀態"` 兩份文件確認。

## 2. CSS 全域 class 沒有做隔離，跨元件同名字會互相蓋掉

- **現象**：`BoardListPage.css` 的 `.board-card__delete`（看板列表的刪除按鈕）跟 `Board.css` 的 `.board-card__delete`（看板格子內卡片右上角的小 × 按鈕）撞名；因為兩份 CSS 都被同一個 bundle 全域載入，後載入的規則贏，看板列表的刪除按鈕被套上 18×18px、`position:absolute` 的小圓鈕樣式，文字被擠成兩行直排。
- **根因**：專案沒有用 CSS Modules／scoped style，純靠 class 命名人工避免碰撞；`.board-card`／`.board-card__delete` 這種名字在「看板列表的一張卡」跟「Kanban 格子裡的一張卡」兩種情境下都很自然，容易各自取到同一個名字。
- **規則**：
  1. 遇到「按鈕/區塊排版跟預期不一樣、但程式碼邏輯看起來沒問題」時，第一件事是用瀏覽器 devtools（或 `page.evaluate` 掃 `document.styleSheets`）查有沒有同名規則從別的檔案跑進來，不要只看自己改的那份 CSS。
  2. 命名跨頁面共用的簡稱（`board-card`、`item`、`avatar`）前，先 grep 全專案確認沒有別的元件已經用了同一個字首。
  3. 修法是重新命名到不會撞（例如 `.board-list-card__delete`），不是加 `!important` 或提高特異度硬蓋過去。

## 3. CSS Grid 的 `align-content` 預設值會把「多的高度」平分給每一列，不是只給看起來需要的那一列

- **現象**：user 回報「待辦/進行中/完成 標題太高」。一開始以為是標題文字換行的問題，加了 `-webkit-line-clamp: 2` 還是沒解決；截圖量出來 `.board-grid__stage-header` 高度 260.5px，跟下面唯一一條泳道的高度完全一樣。
- **根因**：`.board-grid { flex-grow:1; display:grid; grid-auto-rows: minmax(90px, auto); }`——容器高度被外層 flex 撐開到比「所有列自然高度總和」還高時，`align-content` 預設值（`normal`，等同 `stretch`）會把多出來的空間**平均分給每一列**，包含只需要放一行文字的標題列。泳道數愈少，多出來的空間愈明顯，看起來就像「標題被撐高」，其實是空白被塞錯地方。
- **規則**：
  1. 「某一列看起來太高／內容跟容器高度對不上」時，先用 `getComputedStyle(gridEl).gridTemplateRows` 量實際算出來的列高，跟預期的 `minmax(...)` 下限比對，不要先假設是內容撐大的。
  2. 只想要「內容多的列自然變高、內容少的列維持最小值、多的空間留在最後」時，加 `align-content: start`（不要用 `stretch`／預設值）。
  3. 這類 bug 用純文字描述很難覆現，靠 Playwright 截圖＋`boundingBox()` 量測比用肉眼猜快很多。

## 4. Canvas item 的 mousedown 攔截，看的是 DOM 樹的祖先鏈，不是畫面上的堆疊順序

同一個根因在這次對話裡連續抓到兩次：

- **案例 A（卡片拖不動）**：「看板本體」item 外層 `.canvas-item` 綁了 `onMouseDown` 來做「拖動整個 item」；卡片本身（`.board-card`，`draggable=true`，靠原生 HTML5 拖放跨 Stage 移動）是它的 DOM 子孫。點卡片時，mousedown 先冒泡到外層，`handleItemMouseDown` 判斷「item 可移動」就呼叫 `startDrag()` → `e.preventDefault()`。瀏覽器規格是：mousedown 被 `preventDefault()` 過，後續就不會再判定「使用者正在拖曳這個子元素」——原生拖放整個失效，卡片變成完全拖不動。
- **案例 B（新增卡片對話框點不進輸入框）**：`CardAddDialog` 用 `position:fixed` 蓋滿全螢幕，看起來浮在畫布最上層；但它是在 `BoardItemContent`（也就是「看板本體」item 的內容）的 React 樹裡渲染出來的，DOM 上仍然是 `.canvas-item` 的子孫。點輸入框一樣冒泡到外層 `handleItemMouseDown`，一樣被當成「使用者要拖動這個 item」、一樣 `preventDefault()`，這次順便把輸入框該拿到的 focus 也吃掉了——使用者看到的是「點不進輸入欄位，反而把整個看板拖走了」。
- **根因**：`position:fixed` 加高 `z-index` 只決定**畫面上看起來**蓋在哪一層，完全不影響 DOM 事件冒泡的路徑；冒泡永遠沿著實際的 DOM 父子關係走。只要一個「可拖動的容器」外層綁了 mousedown/dragstart 類的全域攔截，它底下任何子孫元素（不管視覺上是不是已經飛到別的地方去了）都會先經過那個攔截。
- **規則**：
  1. 「容器」（這裡是 canvas item）的全域滑鼠攔截器，要在最前面明確排除掉「這次點擊其實是某個獨立互動元件」的情況，不要只靠 `stopPropagation()`（那只擋得住「繼續往上」，擋不住「攔截器自己被觸發」）。這次用兩條規則守：
     - `e.target.closest('[draggable="true"]')`：點到原生可拖曳元素本身，讓瀏覽器自己處理。
     - `e.target.closest('.dialog-backdrop')`：點擊落在任何 modal 對話框裡面，直接放行，不選取也不開始拖曳整個 item。
  2. 只要「浮動/模態內容渲染在某個可拖動容器的 React 樹裡」這個結構還在，任何新加的對話框、任何新加的可拖放子元件都會自動繼承這個坑；不需要每加一個新元件就重新想一次，因為守則是掛在容器層、不是掛在個別子元件上。
  3. 這類 bug 光看程式碼很難發現（邏輯看起來各自獨立），必須實際操作／自動化操作＋斷言（例如量 `input.value` 是不是真的被打進去）才會現形；純靠閱讀 code review 大概率會漏掉。

## 5. React state 用「整包物件換掉」回填非同步結果，兩個操作疊在一起時會互相蓋掉

- **現象**：e2e 測試裡「拖曳移動 item」緊接著「拖曳調整大小」，resize 的結果偶爾會消失（寬高變回拖曳前的值）。
- **根因**：`commitMoveDrag`／`commitResizeDrag` 都是「本地先樂觀更新，然後打 API，拿到伺服器回應後用回應**整包蓋掉**這個 item 的 state」。如果使用者在 move 的 API 回應還沒回來之前就開始下一個 resize 動作，resize 已經把新的 width/height 寫進 state 了，晚到的 move 回應（body 裡還是 resize 之前的舊 width/height）一蓋下去，resize 的結果就被吃掉。
- **規則**：非同步回填 state 時，只覆蓋「這次操作真的负责的欄位」（move 只回填 x/y、resize 只回填 x/y/width/height），不要整包 `{...updated}` 換掉，避免不相關欄位被過期回應覆蓋。這個模式在任何「同一筆資料有多種局部更新操作、且都是非同步」的地方都適用，不是 canvas item 專屬。

## 6. Canvas item 之間互不相通，「資料改了但另一個 item 沒更新」要靠版本號廣播

- **現象**：user 回報「workload 的內容，我對卡片的 owner 做了修改之後，沒有及時做更新」。
- **根因**：每個 canvas item 是獨立掛載的 React 元件樹（`resolveItemComponent` 各自 `useEffect` 只在自己掛載時打一次 API），彼此不共用 state、也看不到對方的變動。這不是 workload 專屬的 bug：WIP、Cycle/Lead Time、Throughput/CFD、截止日期提醒、活動紀錄、Feature/CR 追蹤表全部都有同一個根因。
- **規則**：
  1. 共同祖先（`BoardCanvasPage`）已經有 `BoardContext` 在做跨 item 橋接（原本只有 `requestedCardId` 這一個用途），同一個機制可以擴充：加一個 `cardsVersion`（遞增計數器）＋ `notifyCardsChanged()`，「看板本體」item 每次改完卡片就喊一次，其他讀卡片資料的 item 把 `cardsVersion` 放進自己 `useEffect` 的 dependency array 就會自動重抓。
  2. 這個模式**不能**無腦套用在所有讀卡片資料的 useEffect 上：`DueDateReminderWidget` 有使用者自訂的「套用」門檻天數，如果直接把 `cardsVersion` 加進原本「只在掛載時查一次預設值」的 effect，會在卡片變動時把使用者已經套用的自訂門檻悄悄重置回預設值。正確做法是把「目前查詢用的門檻」拆成獨立 state（`appliedThreshold`），`cardsVersion` 觸發的是「用目前這個門檻重新查」，不是「查詢用回預設值」。
  3. 加新的「讀卡片資料」item 時，順手把 `cardsVersion` 接上，不要等下一次使用者回報「沒有同步」才修。

## 7. 「下拉選單資料來源」用 registry 動態產生，不要在 module 頂層算好

- **現象**：把「加入元件」對話框的元件識別碼從自由輸入改成下拉選單，資料來自 `itemComponentRegistry.ts` 的 `registerItemComponent` 呼叫。第一版把選單清單算成 `PlaceItemDialog.tsx` 的 module 頂層常數（`const COMPONENT_OPTIONS = listRegisteredComponents(...)`），結果某個選項（`board-members`）選了選不到、送出的 `component` 永遠是空字串，e2e 沒抓到，但一個原本用「真實 App 註冊」的既有單元測試（`BoardCanvasPage.test.tsx`）馬上炸了。
- **根因**：`registerItemComponent` 是散落在多個模組（`main.tsx`、`BoardItemContent.tsx`、`BoardMembersItem.tsx`、`registerKanbanWidgets.ts`……）的 side-effect import，各自在被 import 時才登記進 registry；`PlaceItemDialog.tsx` 被哪個模組先 import、什麼時候被 import，會影響它的頂層常數執行時 registry 裡已經登記了哪些——`COMPONENT_OPTIONS` 拿到的是「當下」的半成品清單，不是「最終」的完整清單。
- **規則**：任何依賴多處 side-effect import 累積出來的 registry／清單，讀取時機要延到**真正需要用到的那一刻**（元件掛載、對話框打開），不要在 import 當下、module 頂層就算好存起來。這裡的修法是把 `listRegisteredComponents()` 改成在 `PlaceItemDialog` 元件內用 `useState(() => ...)` 的 lazy initializer 呼叫——這時候整個 App 的 import graph 早就跑完了，registry 保證是完整的。

## 8. 前端手動驗證的做法：Playwright 當截圖／量測工具，不是只拿來寫 e2e 測試

這次沒有裝 Playwright test runner 之前，也一路用單純的 `chromium.launch()` script（不掛 `@playwright/test`）當「快速驗證」工具：

- 截圖比對原型（第 1 條）、量 `boundingBox()`／`getComputedStyle()` 找版面根因（第 3 條）、用 `fireEvent`／`page.mouse` 序列重現「點不到、拖不動」（第 4、5 條）。
- 這類腳本用完即丟，寫在 `.diag*.mjs`（session 工作目錄），修完就刪，不要留在 repo 裡；跟正式的 `e2e/*.spec.ts`（用 `@playwright/test`、有 fixtures、會留著長期跑）分開。
- 兩個環境相關的坑，跟前端修 bug 本身沒關係但每次都要重踩：
  1. **WSL 下 `/mnt/d` 是 Windows 掛載，Vite 的檔案監聽不可靠**——改完檔案有時候要手動 kill + 重啟 `npm run dev` 才會反映，不能假設「存檔就會生效」。
  2. **背景重啟服務時，`pkill; sleep; nohup ... &` 這種複合指令常常因為中間某個指令的 exit code 非 0 而整條沒真的執行**，容易留下「舊的 process 還在跑、新的沒起來」或「兩個 process 同時綁同一個 port」的狀態，畫面看起來正常但其實吃到舊程式碼。每次重啟後要 `ps aux | grep <process>` 確認**剛好一個**、`curl` 確認真的有回應，不要只看指令有沒有報錯。

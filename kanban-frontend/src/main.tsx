import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { ActivityLogItem } from './canvas/ActivityLogItem'
import { FeatureCrBoardItem } from './canvas/FeatureCrBoardItem'
import { registerItemComponent } from './canvas/itemComponentRegistry'

// T-16：s-activity-log 掛上 Canvas item.component === 's-activity-log'（見 ActivityLogItem.tsx 開頭註解；
// item.component 命名慣例改為完整 Screen ID，見 ADR-T-17-fe-clock-control-01「修正」段）。
registerItemComponent('s-activity-log', ActivityLogItem)
// T-20：s-feature-cr-board 掛上 Canvas item.component === 's-feature-cr-board'（見 FeatureCrBoardItem.tsx 開頭註解）。
registerItemComponent('s-feature-cr-board', FeatureCrBoardItem)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)

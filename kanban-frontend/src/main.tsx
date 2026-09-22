import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { ActivityLogItem } from './canvas/ActivityLogItem'
import { FeatureCrBoardItem } from './canvas/FeatureCrBoardItem'
import { registerItemComponent } from './canvas/itemComponentRegistry'

// T-16：s-activity-log 掛上 Canvas item.component === 'activity-log'（見 ActivityLogItem.tsx 開頭註解）。
registerItemComponent('activity-log', ActivityLogItem)
// T-20：s-feature-cr-board 掛上 Canvas item.component === 'feature-cr-board'（見 FeatureCrBoardItem.tsx 開頭註解）。
registerItemComponent('feature-cr-board', FeatureCrBoardItem)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)

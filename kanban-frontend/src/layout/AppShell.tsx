import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import './AppShell.css';

// 對應 ui-user-membership.md 提到的「全域導覽列」：附掛於已登入畫面，顯示 user.display-name、提供登出動作，
// 依 spec 不出現在 s-login／s-signup 本身，所以只包在 ProtectedRoute 之下（CR-007：TopBar 顯示顯示名字而非帳號 ID）。
// 版面依 BoardList.dc.html 上方的 TopBar（品牌標記＋頭像＋顯示名字＋登出），行為不變。
export function AppShell() {
  const { displayName, logout } = useAuth();
  const initial = displayName === null || displayName === '' ? '' : displayName.charAt(0);

  return (
    <div>
      <header className="app-topbar">
        <div className="app-topbar__brand">
          <span className="app-topbar__logo" aria-hidden="true">
            <span />
            <span />
            <span />
            <span />
            <span />
            <span />
            <span />
            <span />
            <span />
          </span>
          <span className="app-topbar__product-name">[產品名稱]</span>
        </div>
        <div className="app-topbar__actions">
          <div className="app-topbar__user">
            <span className="avatar" aria-hidden="true">
              {initial}
            </span>
            <span>{displayName}</span>
          </div>
          <div className="app-topbar__divider" aria-hidden="true" />
          <button type="button" className="btn-sm" onClick={() => void logout()}>
            登出
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}

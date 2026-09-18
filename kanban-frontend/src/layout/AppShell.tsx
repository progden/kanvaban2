import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

// 對應 ui-user-membership.md 提到的「全域導覽列」：附掛於已登入畫面，顯示 user.display-name、提供登出動作，
// 依 spec 不出現在 s-login／s-signup 本身，所以只包在 ProtectedRoute 之下（CR-007：TopBar 顯示顯示名字而非帳號 ID）。
export function AppShell() {
  const { displayName, logout } = useAuth();

  return (
    <div>
      <header>
        <span>{displayName}</span>
        <button type="button" onClick={() => void logout()}>
          登出
        </button>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}

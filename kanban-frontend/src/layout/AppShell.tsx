import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

// 對應 ui-user-membership.md 提到的「全域導覽列」：附掛於已登入畫面，顯示帳號名稱、提供登出動作，
// 依 spec 不出現在 s-login／s-signup 本身，所以只包在 ProtectedRoute 之下。
export function AppShell() {
  const { username, logout } = useAuth();

  return (
    <div>
      <header>
        <span>{username}</span>
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

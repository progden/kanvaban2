import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './useAuth';

// 依 ui-user-membership.md「未登入時的應用程式入口」：未登入使用者不可存取任何看板相關畫面，一律導向 /login。
export function ProtectedRoute() {
  const { status } = useAuth();

  if (status === 'loading') {
    return <div>載入中…</div>;
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}

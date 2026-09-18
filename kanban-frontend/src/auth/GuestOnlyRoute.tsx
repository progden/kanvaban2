import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './useAuth';

// s-login／s-signup 只給未登入使用者；已登入時直接進 s-board-list（本任務先用 /boards 佔位，見 T-12）。
export function GuestOnlyRoute() {
  const { status } = useAuth();

  if (status === 'loading') {
    return <div>載入中…</div>;
  }

  if (status === 'authenticated') {
    return <Navigate to="/boards" replace />;
  }

  return <Outlet />;
}

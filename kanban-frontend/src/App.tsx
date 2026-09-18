import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { GuestOnlyRoute } from './auth/GuestOnlyRoute';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { useAuth } from './auth/useAuth';
import { AppShell } from './layout/AppShell';
import { BoardCanvasPage } from './pages/BoardCanvasPage';
import { BoardListPage } from './pages/BoardListPage';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';

// 依 ui-user-membership.md s-login「進入與離開」：應用程式入口依登入態決定導向 /login 或 /boards。
function RootRedirect() {
  const { status } = useAuth();

  if (status === 'loading') {
    return <div>載入中…</div>;
  }

  return <Navigate to={status === 'authenticated' ? '/boards' : '/login'} replace />;
}

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<RootRedirect />} />
        <Route element={<GuestOnlyRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route path="/boards" element={<BoardListPage />} />
            <Route path="/boards/:boardId" element={<BoardCanvasPage />} />
          </Route>
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;

// s-login：依 ui-user-membership.md 操作表／驗收條件實作；版面未對照設計稿（Login.dc.html，見 OQ-IMPL-12），
// 先用最簡潔可用的表單版面。
import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../api/http';
import { useAuth } from '../auth/useAuth';

export function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      await login(username, password);
      setError(null);
    } catch (e) {
      // 依 uc-login fail-p1／fail-p2：帳號 ID 與密碼欄位不變，顯示訊息，停留本畫面。
      setError(e instanceof ApiError ? e.message : '登入失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1>登入</h1>
      <form onSubmit={(event) => void handleSubmit(event)}>
        <label>
          帳號 ID
          <input
            type="text"
            value={username}
            autoComplete="username"
            onChange={(event) => setUsername(event.target.value)}
          />
        </label>
        <label>
          密碼
          <input
            type="password"
            value={password}
            autoComplete="current-password"
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        {error !== null && <p role="alert">{error}</p>}
        <button type="submit" disabled={submitting}>
          登入
        </button>
      </form>
      <Link to="/signup">前往建立帳號</Link>
    </div>
  );
}

// s-login：依 ui-user-membership.md 操作表／驗收條件實作；版面依 Login.dc.html（見 README「檔案與 Screen ID」），
// 只調整版面與樣式，行為不變。
import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../api/http';
import { useAuth } from '../auth/useAuth';
import './AuthPage.css';

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
    <div className="auth-layout">
      <div className="auth-brand">
        <div className="auth-brand__logo">
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
        <div className="auth-brand__mid">
          <p className="auth-brand__tagline">把看板放到一張畫不完的桌子上。</p>
          <div className="auth-brand__illustration" aria-hidden="true">
            <div className="auth-brand__illustration-tabs">
              <span />
              <span />
              <span />
            </div>
            <div className="auth-brand__illustration-cards">
              <span className="is-active" />
              <span />
              <span />
              <span />
              <span className="is-active" />
              <span />
            </div>
          </div>
        </div>
        <div className="auth-brand__spacer" aria-hidden="true" />
      </div>

      <div className="auth-form">
        <h1>登入</h1>
        <p className="auth-form__subtitle">輸入帳號 ID 與密碼繼續。</p>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <form onSubmit={(event) => void handleSubmit(event)}>
          <div className="field">
            <label className="field-label" htmlFor="login-username">
              帳號 ID
            </label>
            <input
              id="login-username"
              className="field-input"
              type="text"
              value={username}
              autoComplete="username"
              onChange={(event) => setUsername(event.target.value)}
            />
          </div>
          <div className="field">
            <label className="field-label" htmlFor="login-password">
              密碼
            </label>
            <input
              id="login-password"
              className="field-input"
              type="password"
              value={password}
              autoComplete="current-password"
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <button type="submit" className="btn auth-form__submit" disabled={submitting}>
            登入
          </button>
        </form>

        <div className="auth-form__footer">
          還沒有帳號？<Link to="/signup">前往建立帳號</Link>
        </div>
      </div>
    </div>
  );
}

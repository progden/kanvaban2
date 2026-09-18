// s-signup：依 ui-user-membership.md 操作表／驗收條件實作；版面依 Signup.dc.html（見 README「檔案與 Screen ID」），
// 只調整版面與樣式，行為不變。
// 帳號 ID 留空、密碼超過 40 字兩種情況在前端就能判斷，依驗收條件「不觸發 uc-create-user」在送出前擋下，
// 訊息沿用 uc-create-user fail-p3／fail-p1 原文；帳號重複與其他情況仍要送到後端才知道，交由 ApiError 顯示。
import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createUser } from '../api/authApi';
import { ApiError } from '../api/http';
import './AuthPage.css';

const MAX_PASSWORD_LENGTH = 40;
const USERNAME_BLANK_MESSAGE = '使用者名稱不能為空';
const PASSWORD_TOO_LONG_MESSAGE = '密碼長度不可超過 40 個字';

export function SignupPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (username.trim() === '') {
      setError(USERNAME_BLANK_MESSAGE);
      return;
    }
    if (password.length > MAX_PASSWORD_LENGTH) {
      setError(PASSWORD_TOO_LONG_MESSAGE);
      return;
    }

    setSubmitting(true);
    try {
      await createUser(username, displayName, password);
      navigate('/login', { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '建立帳號失敗，請稍後再試');
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
        <div className="auth-brand__note">
          <p className="auth-brand__note-title">帳號建立後無法刪除</p>
          <ul>
            <li>帳號 ID 全系統唯一，之後不能改。</li>
            <li>顯示名字留空時，會直接使用帳號 ID。</li>
            <li>密碼可以留空。</li>
          </ul>
        </div>
      </div>

      <div className="auth-form">
        <h1>建立帳號</h1>
        <p className="auth-form__subtitle">建立完成後會回到登入頁。</p>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <form onSubmit={(event) => void handleSubmit(event)}>
          <div className="field">
            <label className="field-label" htmlFor="signup-username">
              帳號 ID
            </label>
            <input
              id="signup-username"
              className="field-input"
              type="text"
              value={username}
              autoComplete="username"
              onChange={(event) => setUsername(event.target.value)}
            />
          </div>
          <div className="field">
            <span className="field-label">
              <label htmlFor="signup-display-name">顯示名字</label>{' '}
              <span className="field-hint">・選填</span>
            </span>
            <input
              id="signup-display-name"
              className="field-input"
              type="text"
              value={displayName}
              autoComplete="nickname"
              placeholder="留空時等於帳號 ID"
              onChange={(event) => setDisplayName(event.target.value)}
            />
          </div>
          <div className="field">
            <span className="field-label">
              <label htmlFor="signup-password">密碼</label>{' '}
              <span className="field-hint">・選填，可留白</span>
            </span>
            <input
              id="signup-password"
              className="field-input"
              type="password"
              value={password}
              autoComplete="new-password"
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <button type="submit" className="btn auth-form__submit" disabled={submitting}>
            確認建立帳號
          </button>
        </form>

        <div className="auth-form__footer">
          <Link to="/login">已經有帳號？前往登入</Link>
        </div>
      </div>
    </div>
  );
}

// s-signup：依 ui-user-membership.md 操作表／驗收條件實作；版面未對照設計稿（Signup.dc.html，見 OQ-IMPL-12），
// 先用最簡潔可用的表單版面。
// 帳號 ID 留空、密碼超過 40 字兩種情況在前端就能判斷，依驗收條件「不觸發 uc-create-user」在送出前擋下，
// 訊息沿用 uc-create-user fail-p3／fail-p1 原文；帳號重複與其他情況仍要送到後端才知道，交由 ApiError 顯示。
import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createUser } from '../api/authApi';
import { ApiError } from '../api/http';

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
    <div>
      <h1>建立帳號</h1>
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
          顯示名字
          <input
            type="text"
            value={displayName}
            autoComplete="nickname"
            onChange={(event) => setDisplayName(event.target.value)}
          />
        </label>
        <label>
          密碼
          <input
            type="password"
            value={password}
            autoComplete="new-password"
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        {error !== null && <p role="alert">{error}</p>}
        <button type="submit" disabled={submitting}>
          確認建立帳號
        </button>
      </form>
      <Link to="/login">已經有帳號？前往登入</Link>
    </div>
  );
}

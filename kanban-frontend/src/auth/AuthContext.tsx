import { useCallback, useEffect, useState, type ReactNode } from 'react';
import * as authApi from '../api/authApi';
import { AuthContext, type AuthState } from './auth-context';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading', username: null, displayName: null });

  useEffect(() => {
    authApi
      .fetchSession()
      .then((session) =>
        setState({ status: 'authenticated', username: session.username, displayName: session.displayName }),
      )
      .catch(() => setState({ status: 'unauthenticated', username: null, displayName: null }));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const user = await authApi.login(username, password);
    setState({ status: 'authenticated', username: user.username, displayName: user.displayName });
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setState({ status: 'unauthenticated', username: null, displayName: null });
  }, []);

  return <AuthContext.Provider value={{ ...state, login, logout }}>{children}</AuthContext.Provider>;
}

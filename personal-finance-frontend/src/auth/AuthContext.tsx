import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api/authApi'
import type { AuthSession, LoginInput, RegisterInput } from '../types'
import {
  AUTH_UNAUTHORIZED_EVENT,
  clearAuthSession,
  getAuthSession,
  saveAuthSession,
} from './authStorage'

interface AuthContextValue {
  session: AuthSession | null
  isAuthenticated: boolean
  login: (input: LoginInput) => Promise<void>
  register: (input: RegisterInput) => Promise<AuthSession>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => getAuthSession())

  useEffect(() => {
    const handleUnauthorized = () => setSession(null)
    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: session !== null,
    async login(input) {
      const nextSession = await authApi.login(input)
      saveAuthSession(nextSession)
      setSession(nextSession)
    },
    async register(input) {
      const nextSession = await authApi.register(input)
      const authenticatedSession: AuthSession = {
        accessToken: nextSession.accessToken,
        tokenType: nextSession.tokenType,
        expiresIn: nextSession.expiresIn,
        user: nextSession.user,
      }
      saveAuthSession(authenticatedSession)
      setSession(authenticatedSession)
      return nextSession
    },
    logout() {
      clearAuthSession()
      setSession(null)
    },
  }), [session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}

import type { AuthSession } from '../types'

const AUTH_STORAGE_KEY = 'personal-finance-auth'
export const AUTH_UNAUTHORIZED_EVENT = 'personal-finance:unauthorized'

export function getAuthSession(): AuthSession | null {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!stored) return null

  try {
    const session = JSON.parse(stored) as AuthSession
    if (!session.accessToken || !session.user?.id) {
      clearAuthSession()
      return null
    }
    return session
  } catch {
    clearAuthSession()
    return null
  }
}

export function saveAuthSession(session: AuthSession) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function getCurrentUserId(): number {
  const userId = getAuthSession()?.user.id
  if (!userId) throw new Error('You must sign in before using this feature')
  return userId
}

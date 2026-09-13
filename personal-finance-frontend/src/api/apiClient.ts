import axios from 'axios'
import { AUTH_UNAUTHORIZED_EVENT, clearAuthSession, getAuthSession } from '../auth/authStorage'
import { appConfig } from '../config/appConfig'
import type { ApiErrorResponse } from '../types'

export const apiClient = axios.create({ baseURL: appConfig.apiBaseUrl, headers: { 'Content-Type': 'application/json' }, timeout: 15_000 })

apiClient.interceptors.request.use(config => {
  const token = getAuthSession()?.accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (axios.isAxiosError(error) && error.response?.status === 401 && getAuthSession()) {
      clearAuthSession()
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))
    }
    return Promise.reject(error)
  },
)

export function getApiErrorMessage(error: unknown): string {
  if (!axios.isAxiosError<ApiErrorResponse>(error)) return error instanceof Error ? error.message : 'Something went wrong'
  const response = error.response?.data
  if (response?.data && Object.keys(response.data).length > 0) return Object.values(response.data).join('. ')
  return response?.message || error.message || 'Unable to reach the server'
}

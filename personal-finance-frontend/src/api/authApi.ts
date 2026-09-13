import type { ApiResponse, AuthSession, LoginInput, RegisterInput, ResetPasswordInput } from '../types'
import { apiClient } from './apiClient'

export const authApi = {
  async login(input: LoginInput) {
    return (await apiClient.post<ApiResponse<AuthSession>>('/api/auth/login', input)).data.data
  },

  async register(input: RegisterInput) {
    return (await apiClient.post<ApiResponse<AuthSession>>('/api/auth/register', input)).data.data
  },

  async resetPassword(input: ResetPasswordInput) {
    return (await apiClient.post<ApiResponse<null>>('/api/auth/reset-password', input)).data
  },
}

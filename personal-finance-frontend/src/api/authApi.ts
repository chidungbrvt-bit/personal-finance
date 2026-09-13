import type { ApiResponse, AuthSession, LoginInput, RegisterInput } from '../types'
import { apiClient } from './apiClient'

export const authApi = {
  async login(input: LoginInput) {
    return (await apiClient.post<ApiResponse<AuthSession>>('/api/auth/login', input)).data.data
  },

  async register(input: RegisterInput) {
    return (await apiClient.post<ApiResponse<AuthSession>>('/api/auth/register', input)).data.data
  },
}

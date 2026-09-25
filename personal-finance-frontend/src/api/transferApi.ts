import { getCurrentUserId } from '../auth/authStorage'
import type { AccountTransfer, AccountTransferInput, ApiResponse } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/transfers`

export const transferApi = {
  async getAll() { return (await apiClient.get<ApiResponse<AccountTransfer[]>>(basePath())).data.data },
  async create(input: AccountTransferInput) { return (await apiClient.post<ApiResponse<AccountTransfer>>(basePath(), input)).data.data },
}

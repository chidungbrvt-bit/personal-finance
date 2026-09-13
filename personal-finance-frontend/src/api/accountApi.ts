import { getCurrentUserId } from '../auth/authStorage'
import type { Account, AccountInput, ApiResponse } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/accounts`
export const accountApi = {
  async getAll() { return (await apiClient.get<ApiResponse<Account[]>>(basePath())).data.data },
  async getArchived() { return (await apiClient.get<ApiResponse<Account[]>>(`${basePath()}/archived`)).data.data },
  async getBalance(accountId: number) { return (await apiClient.get<ApiResponse<number>>(`${basePath()}/${accountId}/balance`)).data.data },
  async create(input: AccountInput) { return (await apiClient.post<ApiResponse<Account>>(basePath(), input)).data.data },
  async update(accountId: number, input: AccountInput) { return (await apiClient.put<ApiResponse<Account>>(`${basePath()}/${accountId}`, input)).data.data },
  async archive(accountId: number) { await apiClient.delete(`${basePath()}/${accountId}`) },
  async restore(accountId: number) { return (await apiClient.patch<ApiResponse<Account>>(`${basePath()}/${accountId}/restore`)).data.data },
}

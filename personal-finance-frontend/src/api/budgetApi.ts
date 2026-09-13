import { getCurrentUserId } from '../auth/authStorage'
import type { ApiResponse, Budget, BudgetInput, BudgetProgress } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/budgets`
export const budgetApi = {
  async getAll(year?: number, month?: number) { return (await apiClient.get<ApiResponse<Budget[]>>(basePath(), { params: year && month ? { year, month } : undefined })).data.data },
  async getProgress(budgetId: number) { return (await apiClient.get<ApiResponse<BudgetProgress>>(`${basePath()}/${budgetId}/progress`)).data.data },
  async create(input: BudgetInput) { return (await apiClient.post<ApiResponse<Budget>>(basePath(), input)).data.data },
  async update(budgetId: number, input: BudgetInput) { return (await apiClient.put<ApiResponse<Budget>>(`${basePath()}/${budgetId}`, input)).data.data },
  async remove(budgetId: number) { await apiClient.delete(`${basePath()}/${budgetId}`) },
}

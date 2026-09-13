import { getCurrentUserId } from '../auth/authStorage'
import type { ApiResponse, FinancialTransaction, FinancialTransactionInput, TransactionFilters } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/transactions`
export const transactionApi = {
  async getAll(filters: TransactionFilters = {}) { return (await apiClient.get<ApiResponse<FinancialTransaction[]>>(basePath(), { params: filters })).data.data },
  async create(input: FinancialTransactionInput) { return (await apiClient.post<ApiResponse<FinancialTransaction>>(basePath(), input)).data.data },
  async update(transactionId: number, input: FinancialTransactionInput) { return (await apiClient.put<ApiResponse<FinancialTransaction>>(`${basePath()}/${transactionId}`, input)).data.data },
  async remove(transactionId: number) { await apiClient.delete(`${basePath()}/${transactionId}`) },
}

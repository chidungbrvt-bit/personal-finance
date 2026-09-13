import { getCurrentUserId } from '../auth/authStorage'
import type { ApiResponse, FinancialSummary, SummaryParams } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/summary`
export const summaryApi = { async get(params: SummaryParams) { return (await apiClient.get<ApiResponse<FinancialSummary>>(basePath(), { params })).data.data } }

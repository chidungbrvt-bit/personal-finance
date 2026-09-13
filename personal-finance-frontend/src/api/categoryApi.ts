import { getCurrentUserId } from '../auth/authStorage'
import type { ApiResponse, Category, CategoryInput } from '../types'
import { apiClient } from './apiClient'

const basePath = () => `/api/users/${getCurrentUserId()}/categories`
export const categoryApi = {
  async getAll() { return (await apiClient.get<ApiResponse<Category[]>>(basePath())).data.data },
  async create(input: CategoryInput) { return (await apiClient.post<ApiResponse<Category>>(basePath(), input)).data.data },
  async update(categoryId: number, input: CategoryInput) { return (await apiClient.put<ApiResponse<Category>>(`${basePath()}/${categoryId}`, input)).data.data },
  async remove(categoryId: number) { await apiClient.delete(`${basePath()}/${categoryId}`) },
}

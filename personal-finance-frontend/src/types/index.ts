export type AccountType = 'CASH' | 'BANK' | 'E_WALLET'
export type CurrencyType = 'VND' | 'USD' | 'EUR'
export type TransactionType = 'INCOME' | 'EXPENSE'
export type PeriodType = 'DAY' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM'

export interface ApiResponse<T> { message: string; httpStatus: string; data: T }
export interface ApiErrorResponse { timestamp?: string; path?: string; message?: string; httpStatus?: string; data?: Record<string, string> }

export type Role = 'USER' | 'ADMIN'
export interface User {
  id: number; name: string; email: string; role: Role
  createdAt: string | null; updatedAt: string | null
}
export interface LoginInput { email: string; password: string }
export interface RegisterInput { name: string; email: string; password: string }
export interface ResetPasswordInput { recoveryCode: string; newPassword: string }
export interface AuthSession {
  accessToken: string; tokenType: 'Bearer'; expiresIn: number; user: User
}

export interface Account {
  id: number; name: string; type: AccountType; initialBalance: number
  currency: CurrencyType; active: boolean; createdAt: string | null
}
export interface AccountInput {
  name: string; type: AccountType; initialBalance: number; currency: 'VND'; active?: boolean
}
export interface Category {
  id: number; name: string; type: TransactionType; description: string | null
  active: boolean; createdAt: string | null
}
export interface CategoryInput { name: string; type: TransactionType; description: string; active?: boolean }
export interface FinancialTransaction {
  id: number; type: TransactionType; amount: number; transactionDate: string
  description: string | null; accountId: number; categoryId: number; createdAt: string | null
}
export interface FinancialTransactionInput {
  type: TransactionType; amount: number; transactionDate: string
  description: string; accountId: number; categoryId: number
}
export interface TransactionFilters {
  accountId?: number; categoryId?: number; type?: TransactionType; period?: PeriodType
  day?: number; month?: number; quarter?: number; year?: number
  fromDate?: string; toDate?: string
}
export interface Budget {
  id: number; year: number; month: number; amount: number; categoryId: number; createdAt: string | null
}
export interface BudgetInput { year: number; month: number; amount: number; categoryId: number }
export interface BudgetProgress {
  budgetId: number; categoryId: number; categoryName: string; year: number; month: number
  budgetAmount: number; spentAmount: number; remainingAmount: number
  percentageUsed: number; overBudget: boolean
}
export interface FinancialSummary {
  period: PeriodType; startDate: string; endDate: string; totalIncome: number
  totalExpenses: number; netAmount: number; incomeByCategory: Record<string, number>
  expensesByCategory: Record<string, number>
}
export interface SummaryParams {
  period: PeriodType; year?: number; month?: number; quarter?: number
  day?: number; fromDate?: string; toDate?: string
}

import type { AccountType, TransactionType } from '../types'

export function formatCurrency(value: number, currency = 'VND') {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency, maximumFractionDigits: currency === 'VND' ? 0 : 2 }).format(value || 0)
}
export function formatDate(value: string) {
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(`${value}T00:00:00`))
}
export function formatAccountType(type: AccountType) { return { CASH: 'Cash', BANK: 'Bank account', E_WALLET: 'E-wallet' }[type] }
export function formatTransactionType(type: TransactionType) { return type === 'INCOME' ? 'Income' : 'Expense' }
export function todayIso() {
  const now = new Date(); const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

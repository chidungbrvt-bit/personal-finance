import { Navigate, Route, Routes } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import { AppLayout } from './layouts/AppLayout'
import { LoadingState } from './components/PageState'
import { ProtectedRoute, PublicOnlyRoute } from './auth/AuthRoutes'
import { LanguageToggle } from './components/LanguageToggle'
import { useLanguage, useTranslation } from './i18n/LanguageContext'

const AccountsPage = lazy(() => import('./pages/AccountsPage').then(module => ({ default: module.AccountsPage })))
const BudgetsPage = lazy(() => import('./pages/BudgetsPage').then(module => ({ default: module.BudgetsPage })))
const CategoriesPage = lazy(() => import('./pages/CategoriesPage').then(module => ({ default: module.CategoriesPage })))
const DashboardPage = lazy(() => import('./pages/DashboardPage').then(module => ({ default: module.DashboardPage })))
const TransactionsPage = lazy(() => import('./pages/TransactionsPage').then(module => ({ default: module.TransactionsPage })))
const LoginPage = lazy(() => import('./pages/LoginPage').then(module => ({ default: module.LoginPage })))
const RegisterPage = lazy(() => import('./pages/RegisterPage').then(module => ({ default: module.RegisterPage })))
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage').then(module => ({ default: module.ResetPasswordPage })))

function App() {
  const t = useTranslation()
  const { language } = useLanguage()
  return (
    <><LanguageToggle /><Suspense key={language} fallback={<div className="p-6"><LoadingState label={t('Opening page…')} /></div>}><Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
      </Route>
      <Route path="forgot-password" element={<Navigate to="/reset-password" replace />} />
      <Route path="reset-password" element={<ResetPasswordPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="accounts" element={<AccountsPage />} />
          <Route path="categories" element={<CategoriesPage />} />
          <Route path="transactions" element={<TransactionsPage />} />
          <Route path="budgets" element={<BudgetsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes></Suspense></>
  )
}

export default App

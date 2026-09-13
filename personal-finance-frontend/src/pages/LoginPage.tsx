import { Eye, EyeOff, LockKeyhole, WalletCards } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiClient'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login({ email, password })
      navigate('/dashboard', { replace: true })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-screen bg-slate-950 lg:grid-cols-[1.05fr_0.95fr]">
      <section className="hidden flex-col justify-between overflow-hidden p-12 lg:flex">
        <div className="flex items-center gap-3 text-white">
          <span className="grid size-11 place-items-center rounded-2xl bg-emerald-500"><WalletCards size={23} /></span>
          <span className="text-xl font-bold">Evergreen Finance</span>
        </div>
        <div className="max-w-xl">
          <p className="text-sm font-bold uppercase tracking-[0.22em] text-emerald-400">Personal finance, clearly</p>
          <h1 className="mt-5 text-5xl font-bold leading-tight text-white">Know where your money goes.</h1>
          <p className="mt-5 text-lg leading-8 text-slate-400">Track accounts, transactions and monthly budgets from one calm, private dashboard.</p>
        </div>
        <p className="text-sm text-slate-500">All amounts are managed in VND.</p>
      </section>

      <section className="flex items-center justify-center bg-[#f6f8f7] px-5 py-10 sm:px-10">
        <div className="w-full max-w-md">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <span className="grid size-10 place-items-center rounded-2xl bg-emerald-600 text-white"><WalletCards size={21} /></span>
            <span className="font-bold text-slate-950">Evergreen Finance</span>
          </div>
          <div className="panel p-6 sm:p-8">
            <span className="stat-icon stat-icon-emerald"><LockKeyhole size={19} /></span>
            <h2 className="mt-5 text-3xl font-bold tracking-tight text-slate-950">Welcome back</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">Sign in to continue to your financial dashboard.</p>

            {error && <div role="alert" className="mt-5 rounded-xl border border-rose-100 bg-rose-50 p-3 text-sm font-medium text-rose-700">{error}</div>}

            <form onSubmit={submit} className="mt-6 space-y-4">
              <label>
                <span className="field-label">Email</span>
                <input className="field-input" type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" />
              </label>
              <label>
                <span className="field-label">Password</span>
                <span className="relative block">
                  <input className="field-input pr-12" type={showPassword ? 'text' : 'password'} autoComplete="current-password" required value={password} onChange={event => setPassword(event.target.value)} placeholder="Your password" />
                  <button type="button" className="absolute inset-y-0 right-0 grid w-12 place-items-center text-slate-400 transition hover:text-slate-700" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'} aria-pressed={showPassword}>
                    {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                  </button>
                </span>
              </label>
              <div className="text-right"><Link className="text-sm font-bold text-emerald-700 hover:text-emerald-800" to="/forgot-password">Forgot password?</Link></div>
              <button className="button-primary w-full" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</button>
            </form>

            <p className="mt-6 text-center text-sm text-slate-500">New here? <Link className="font-bold text-emerald-700 hover:text-emerald-800" to="/register">Create an account</Link></p>
          </div>
        </div>
      </section>
    </main>
  )
}

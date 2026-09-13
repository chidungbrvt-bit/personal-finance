import { KeyRound, WalletCards } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { getApiErrorMessage } from '../api/apiClient'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)
    try {
      const response = await authApi.forgotPassword(email)
      setMessage(response.message)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  return <main className="flex min-h-screen items-center justify-center bg-[#f6f8f7] px-5 py-10">
    <div className="w-full max-w-md">
      <div className="mb-7 flex items-center justify-center gap-3"><span className="grid size-10 place-items-center rounded-2xl bg-emerald-600 text-white"><WalletCards size={21}/></span><span className="font-bold text-slate-950">Evergreen Finance</span></div>
      <section className="panel p-6 sm:p-8">
        <span className="stat-icon stat-icon-emerald"><KeyRound size={19}/></span>
        <h1 className="mt-5 text-3xl font-bold tracking-tight text-slate-950">Forgot password?</h1>
        <p className="mt-2 text-sm leading-6 text-slate-500">Enter your account email and we will send a link to choose a new password.</p>
        {message && <div role="status" className="mt-5 rounded-xl border border-emerald-100 bg-emerald-50 p-3 text-sm font-medium text-emerald-700">{message}</div>}
        {error && <div role="alert" className="mt-5 rounded-xl border border-rose-100 bg-rose-50 p-3 text-sm font-medium text-rose-700">{error}</div>}
        <form className="mt-6 space-y-4" onSubmit={submit}>
          <label><span className="field-label">Email</span><input className="field-input" type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com"/></label>
          <button className="button-primary w-full" disabled={submitting}>{submitting ? 'Sending…' : 'Send reset link'}</button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-500"><Link className="font-bold text-emerald-700 hover:text-emerald-800" to="/login">Back to sign in</Link></p>
      </section>
    </div>
  </main>
}

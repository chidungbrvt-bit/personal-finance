import { Check, Copy, Eye, EyeOff, ShieldCheck, UserPlus, WalletCards } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiClient'
import { useAuth } from '../auth/AuthContext'

export function RegisterPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState('')
  const [recoveryCode, setRecoveryCode] = useState('')
  const [copied, setCopied] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setSubmitting(true)
    try {
      const session = await register({ name, email, password })
      if (!session.recoveryCode) throw new Error('The server did not provide a recovery code')
      setRecoveryCode(session.recoveryCode)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  const copyRecoveryCode = async () => {
    await navigator.clipboard.writeText(recoveryCode)
    setCopied(true)
  }

  return (
    <main className="grid min-h-screen bg-slate-950 lg:grid-cols-[1.05fr_0.95fr]">
      <section className="hidden flex-col justify-between p-12 lg:flex">
        <div className="flex items-center gap-3 text-white">
          <span className="grid size-11 place-items-center rounded-2xl bg-emerald-500"><WalletCards size={23} /></span>
          <span className="text-xl font-bold">Evergreen Finance</span>
        </div>
        <div className="max-w-xl">
          <p className="text-sm font-bold uppercase tracking-[0.22em] text-emerald-400">Start with a clear picture</p>
          <h1 className="mt-5 text-5xl font-bold leading-tight text-white">Build better money habits.</h1>
          <p className="mt-5 text-lg leading-8 text-slate-400">Create your private workspace, then add your first account and category.</p>
        </div>
        <p className="text-sm text-slate-500">Your account is created with the USER role.</p>
      </section>

      <section className="flex items-center justify-center bg-[#f6f8f7] px-5 py-10 sm:px-10">
        <div className="w-full max-w-md">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <span className="grid size-10 place-items-center rounded-2xl bg-emerald-600 text-white"><WalletCards size={21} /></span>
            <span className="font-bold text-slate-950">Evergreen Finance</span>
          </div>
          <div className="panel p-6 sm:p-8">
            <span className="stat-icon stat-icon-emerald">{recoveryCode ? <ShieldCheck size={19} /> : <UserPlus size={19} />}</span>
            <h2 className="mt-5 text-3xl font-bold tracking-tight text-slate-950">{recoveryCode ? 'Save your recovery code' : 'Create your account'}</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">{recoveryCode ? 'This code is shown only once. Store it in a password manager before continuing.' : 'It only takes a minute to get started.'}</p>

            {error && <div role="alert" className="mt-5 rounded-xl border border-rose-100 bg-rose-50 p-3 text-sm font-medium text-rose-700">{error}</div>}

            {recoveryCode ? <div className="mt-6 space-y-4">
              <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm font-medium text-amber-900">If you lose both your password and this code, the app cannot recover your account.</div>
              <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 p-3">
                <code className="min-w-0 flex-1 break-all text-sm font-bold text-slate-900">{recoveryCode}</code>
                <button type="button" className="grid size-10 shrink-0 place-items-center rounded-lg border border-slate-200 bg-white text-slate-600 hover:text-emerald-700" onClick={copyRecoveryCode} aria-label="Copy recovery code">{copied ? <Check size={18} /> : <Copy size={18} />}</button>
              </div>
              <button type="button" className="button-primary w-full" onClick={() => navigate('/dashboard', { replace: true })}>I saved it — continue</button>
            </div> : <form onSubmit={submit} className="mt-6 space-y-4">
              <label><span className="field-label">Name</span><input className="field-input" autoComplete="name" required value={name} onChange={event => setName(event.target.value)} placeholder="Your name" /></label>
              <label><span className="field-label">Email</span><input className="field-input" type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label>
              <label><span className="field-label">Password</span><span className="relative block"><input className="field-input pr-12" type={showPassword ? 'text' : 'password'} autoComplete="new-password" minLength={8} maxLength={72} required value={password} onChange={event => setPassword(event.target.value)} placeholder="8–72 characters" /><button type="button" className="absolute inset-y-0 right-0 grid w-12 place-items-center text-slate-400 transition hover:text-slate-700" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'} aria-pressed={showPassword}>{showPassword ? <EyeOff size={19} /> : <Eye size={19} />}</button></span></label>
              <label><span className="field-label">Confirm password</span><span className="relative block"><input className="field-input pr-12" type={showConfirmPassword ? 'text' : 'password'} autoComplete="new-password" minLength={8} maxLength={72} required value={confirmPassword} onChange={event => setConfirmPassword(event.target.value)} placeholder="Repeat your password" /><button type="button" className="absolute inset-y-0 right-0 grid w-12 place-items-center text-slate-400 transition hover:text-slate-700" onClick={() => setShowConfirmPassword(value => !value)} aria-label={showConfirmPassword ? 'Hide password' : 'Show password'} aria-pressed={showConfirmPassword}>{showConfirmPassword ? <EyeOff size={19} /> : <Eye size={19} />}</button></span></label>
              <button className="button-primary w-full" disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}</button>
            </form>}

            {!recoveryCode && <p className="mt-6 text-center text-sm text-slate-500">Already have an account? <Link className="font-bold text-emerald-700 hover:text-emerald-800" to="/login">Sign in</Link></p>}
          </div>
        </div>
      </section>
    </main>
  )
}

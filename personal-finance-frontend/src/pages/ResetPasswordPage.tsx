import { Eye, EyeOff, KeyRound, WalletCards } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { getApiErrorMessage } from '../api/apiClient'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    if (!token) { setError('The reset link is missing its token.'); return }
    if (password !== confirmPassword) { setError('Passwords do not match'); return }
    setSubmitting(true)
    try {
      await authApi.resetPassword({ token, newPassword: password })
      setSuccess(true)
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
        <h1 className="mt-5 text-3xl font-bold tracking-tight text-slate-950">Choose a new password</h1>
        {success ? <><div role="status" className="mt-5 rounded-xl border border-emerald-100 bg-emerald-50 p-3 text-sm font-medium text-emerald-700">Your password has been reset successfully.</div><Link className="button-primary mt-6 w-full" to="/login">Sign in with new password</Link></> : <>
          <p className="mt-2 text-sm leading-6 text-slate-500">Use between 8 and 72 characters.</p>
          {error && <div role="alert" className="mt-5 rounded-xl border border-rose-100 bg-rose-50 p-3 text-sm font-medium text-rose-700">{error}</div>}
          <form className="mt-6 space-y-4" onSubmit={submit}>
            <label><span className="field-label">New password</span><span className="relative block"><input className="field-input pr-12" type={showPassword?'text':'password'} autoComplete="new-password" minLength={8} maxLength={72} required value={password} onChange={event=>setPassword(event.target.value)} placeholder="8–72 characters"/><button type="button" className="absolute inset-y-0 right-0 grid w-12 place-items-center text-slate-400 hover:text-slate-700" onClick={()=>setShowPassword(value=>!value)} aria-label={showPassword?'Hide password':'Show password'}>{showPassword?<EyeOff size={19}/>:<Eye size={19}/>}</button></span></label>
            <label><span className="field-label">Confirm new password</span><span className="relative block"><input className="field-input pr-12" type={showConfirmPassword?'text':'password'} autoComplete="new-password" minLength={8} maxLength={72} required value={confirmPassword} onChange={event=>setConfirmPassword(event.target.value)} placeholder="Repeat your password"/><button type="button" className="absolute inset-y-0 right-0 grid w-12 place-items-center text-slate-400 hover:text-slate-700" onClick={()=>setShowConfirmPassword(value=>!value)} aria-label={showConfirmPassword?'Hide password':'Show password'}>{showConfirmPassword?<EyeOff size={19}/>:<Eye size={19}/>}</button></span></label>
            <button className="button-primary w-full" disabled={submitting}>{submitting?'Resetting…':'Reset password'}</button>
          </form>
        </>}
      </section>
    </div>
  </main>
}

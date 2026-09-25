import { Archive, ArrowRightLeft, Pencil, Plus, RotateCcw, WalletCards } from 'lucide-react'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { accountApi } from '../api/accountApi'
import { transferApi } from '../api/transferApi'
import { getApiErrorMessage } from '../api/apiClient'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '../components/PageState'
import { useToast } from '../components/ToastProvider'
import type { Account, AccountInput, AccountTransfer, AccountTransferInput, AccountType } from '../types'
import { formatAccountType, formatCurrency, formatDate, todayIso } from '../utils/format'

const blank: AccountInput = { name: '', type: 'BANK', initialBalance: 0, currency: 'VND' }
const blankTransfer = (): AccountTransferInput => ({ sourceAccountId: 0, targetAccountId: 0, amount: 0, transferDate: todayIso(), description: '' })

export function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [archived, setArchived] = useState<Account[]>([])
  const [transfers, setTransfers] = useState<AccountTransfer[]>([])
  const [balances, setBalances] = useState<Record<number, number>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [form, setForm] = useState<AccountInput>(blank)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [deleting, setDeleting] = useState<Account | null>(null)
  const [saving, setSaving] = useState(false)
  const [transferOpen, setTransferOpen] = useState(false)
  const [transferForm, setTransferForm] = useState<AccountTransferInput>(blankTransfer)
  const { showToast } = useToast()

  const load = useCallback(async () => {
    setLoading(true); setError('')
    try {
      const [active, inactive, transferHistory] = await Promise.all([accountApi.getAll(), accountApi.getArchived(), transferApi.getAll()])
      setAccounts(active); setArchived(inactive); setTransfers(transferHistory)
      const entries = await Promise.all(active.map(async (account) => [account.id, await accountApi.getBalance(account.id)] as const))
      setBalances(Object.fromEntries(entries))
    } catch (e) { setError(getApiErrorMessage(e)) } finally { setLoading(false) }
  }, [])

  useEffect(() => { void load() }, [load])
  const openCreate = () => { setEditing(null); setForm(blank); setOpen(true) }
  const openTransfer = () => {
    if (accounts.length < 2) return
    setTransferForm({ ...blankTransfer(), sourceAccountId: accounts[0].id, targetAccountId: accounts[1].id })
    setTransferOpen(true)
  }
  const openEdit = (account: Account) => {
    setEditing(account)
    setForm({ name: account.name, type: account.type, initialBalance: account.initialBalance, currency: 'VND', active: account.active })
    setOpen(true)
  }
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setSaving(true)
    try {
      if (editing) await accountApi.update(editing.id, form); else await accountApi.create(form)
      showToast(`Account ${editing ? 'updated' : 'created'} successfully`); setOpen(false); setEditing(null); setForm(blank); await load()
    } catch (e) { showToast(getApiErrorMessage(e), 'error') } finally { setSaving(false) }
  }
  const remove = async () => {
    if (!deleting) return
    setSaving(true)
    try { await accountApi.archive(deleting.id); showToast('Account removed or archived successfully'); setDeleting(null); await load() }
    catch (e) { showToast(getApiErrorMessage(e), 'error') } finally { setSaving(false) }
  }
  const restore = async (account: Account) => {
    try { await accountApi.restore(account.id); showToast('Account restored'); await load() }
    catch (e) { showToast(getApiErrorMessage(e), 'error') }
  }
  const submitTransfer = async (event: FormEvent) => {
    event.preventDefault(); setSaving(true)
    try {
      await transferApi.create(transferForm)
      showToast('Money transferred successfully')
      setTransferOpen(false); setTransferForm(blankTransfer()); await load()
    } catch (e) { showToast(getApiErrorMessage(e), 'error') } finally { setSaving(false) }
  }
  const accountName = (accountId: number) => [...accounts, ...archived].find(account => account.id === accountId)?.name ?? `Account #${accountId}`
  return <>
    <PageHeader eyebrow="Money sources" title="Accounts" description="Manage cash, bank accounts and e-wallets. All balances use VND." action={<div className="flex flex-wrap gap-2"><button className="button-secondary" disabled={accounts.length < 2} title={accounts.length < 2 ? 'Create at least two active accounts first.' : undefined} onClick={openTransfer}><ArrowRightLeft size={17}/>Transfer money</button><button className="button-primary" onClick={openCreate}><Plus size={17}/>Add account</button></div>} />
    {loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={load}/> : <div className="space-y-7">
      {accounts.length === 0 ? <EmptyState title="No active accounts" description="Create your first account to start tracking money."/> :
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{accounts.map(account => <article className="panel p-5" key={account.id}>
          <div className="flex items-start justify-between"><span className="stat-icon stat-icon-emerald"><WalletCards size={19}/></span><div className="flex gap-2"><button className="icon-button" aria-label="Edit" onClick={() => openEdit(account)}><Pencil size={16}/></button><button className="icon-button" aria-label="Archive" onClick={() => setDeleting(account)}><Archive size={16}/></button></div></div>
          <div className="mt-5 flex items-center justify-between gap-3"><p className="text-sm font-semibold text-slate-500">{formatAccountType(account.type)}</p><span className="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">Active</span></div><h2 className="mt-1 text-xl font-bold text-slate-950">{account.name}</h2>
          <p className="mt-5 text-2xl font-bold text-slate-950">{formatCurrency(balances[account.id] ?? account.initialBalance)}</p><p className="mt-1 text-xs text-slate-400">Current balance · VND</p>
        </article>)}</div>}
      {archived.length > 0 && <section><h2 className="mb-3 text-lg font-bold text-slate-900">Archived accounts</h2><div className="panel divide-y divide-slate-100">{archived.map(account => <div key={account.id} className="flex items-center justify-between gap-4 p-4"><div><p className="font-semibold text-slate-800">{account.name}</p><p className="text-sm text-slate-400">{formatAccountType(account.type)}</p></div><button className="button-secondary" onClick={() => restore(account)}><RotateCcw size={16}/>Restore</button></div>)}</div></section>}
      {transfers.length > 0 && <section><h2 className="mb-3 text-lg font-bold text-slate-900">Recent transfers</h2><div className="panel divide-y divide-slate-100">{transfers.slice(0, 5).map(transfer => <div key={transfer.id} className="flex flex-col justify-between gap-2 p-4 sm:flex-row sm:items-center"><div><p className="font-semibold text-slate-800">{accountName(transfer.sourceAccountId)} <span className="mx-1 text-slate-400">→</span> {accountName(transfer.targetAccountId)}</p><p className="text-sm text-slate-400">{formatDate(transfer.transferDate)}{transfer.description ? ` · ${transfer.description}` : ''}</p></div><p className="font-bold text-slate-950">{formatCurrency(transfer.amount)}</p></div>)}</div></section>}
    </div>}
    <Modal open={open} title={editing ? 'Edit account' : 'New account'} description="Currency is fixed to VND for this version." onClose={() => { setOpen(false); setEditing(null); setForm(blank) }}>
      <form onSubmit={submit} className="space-y-4"><label><span className="field-label">Account name</span><input className="field-input" required maxLength={100} value={form.name} onChange={e => setForm({...form, name:e.target.value})}/></label>
        <label><span className="field-label">Type</span><select className="field-input" value={form.type} onChange={e => setForm({...form, type:e.target.value as AccountType})}><option value="CASH">Cash</option><option value="BANK">Bank account</option><option value="E_WALLET">E-wallet</option></select></label>
        <label><span className="field-label">Initial balance</span><input className="field-input" type="number" step="1" required value={form.initialBalance} onChange={e => setForm({...form, initialBalance:Number(e.target.value)})}/></label>
        <label><span className="field-label">Currency</span><input className="field-input bg-slate-50" value="VND" disabled/></label>
        <div className="flex justify-end gap-3 pt-2"><button type="button" className="button-secondary" onClick={() => { setOpen(false); setEditing(null); setForm(blank) }}>Cancel</button><button className="button-primary" disabled={saving}>{saving?'Saving…':'Save account'}</button></div>
      </form>
    </Modal>
    <Modal open={transferOpen} title="Transfer money" description="Move money between two active accounts without counting it as income or expense." onClose={() => { if (!saving) { setTransferOpen(false); setTransferForm(blankTransfer()) } }}>
      <form onSubmit={submitTransfer} className="space-y-4">
        <label><span className="field-label">From account</span><select className="field-input" required value={transferForm.sourceAccountId || ''} onChange={e => setTransferForm({...transferForm, sourceAccountId:Number(e.target.value)})}><option value="" disabled>Choose source account</option>{accounts.map(account => <option key={account.id} value={account.id}>{account.name} · {formatCurrency(balances[account.id] ?? account.initialBalance)}</option>)}</select></label>
        <label><span className="field-label">To account</span><select className="field-input" required value={transferForm.targetAccountId || ''} onChange={e => setTransferForm({...transferForm, targetAccountId:Number(e.target.value)})}><option value="" disabled>Choose target account</option>{accounts.filter(account => account.id !== transferForm.sourceAccountId).map(account => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        <label><span className="field-label">Amount (VND)</span><input className="field-input" type="number" min="1" step="1" required value={transferForm.amount || ''} onChange={e => setTransferForm({...transferForm, amount:Number(e.target.value)})}/></label>
        <label><span className="field-label">Transfer date</span><input className="field-input" type="date" required value={transferForm.transferDate} onChange={e => setTransferForm({...transferForm, transferDate:e.target.value})}/></label>
        <label><span className="field-label">Description (optional)</span><input className="field-input" maxLength={255} value={transferForm.description} onChange={e => setTransferForm({...transferForm, description:e.target.value})}/></label>
        <div className="flex justify-end gap-3 pt-2"><button type="button" className="button-secondary" disabled={saving} onClick={() => { setTransferOpen(false); setTransferForm(blankTransfer()) }}>Cancel</button><button className="button-primary" disabled={saving || transferForm.sourceAccountId === transferForm.targetAccountId}>{saving?'Transferring…':'Transfer money'}</button></div>
      </form>
    </Modal>
    <ConfirmDialog open={!!deleting} title="Remove account?" message="Accounts with transactions are archived; unused accounts may be permanently deleted." confirmLabel="Remove account" busy={saving} onClose={() => setDeleting(null)} onConfirm={remove}/>
  </>
}

import { Pencil, Plus, Tags, Trash2 } from 'lucide-react'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { getApiErrorMessage } from '../api/apiClient'
import { categoryApi } from '../api/categoryApi'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '../components/PageState'
import { useToast } from '../components/ToastProvider'
import type { Category, CategoryInput, TransactionType } from '../types'

const blank: CategoryInput = { name:'', type:'EXPENSE', description:'' }
export function CategoriesPage() {
  const [items,setItems]=useState<Category[]>([]), [loading,setLoading]=useState(true), [error,setError]=useState('')
  const [form,setForm]=useState<CategoryInput>(blank), [editing,setEditing]=useState<Category|null>(null), [deleting,setDeleting]=useState<Category|null>(null), [open,setOpen]=useState(false), [busy,setBusy]=useState(false)
  const {showToast}=useToast()
  const load=useCallback(async()=>{setLoading(true);setError('');try{setItems(await categoryApi.getAll())}catch(e){setError(getApiErrorMessage(e))}finally{setLoading(false)}},[])
  useEffect(()=>{void load()},[load])
  const edit=(c:Category)=>{setEditing(c);setForm({name:c.name,type:c.type,description:c.description??'',active:c.active});setOpen(true)}
  const submit=async(e:FormEvent)=>{e.preventDefault();setBusy(true);try{if(editing)await categoryApi.update(editing.id,form);else await categoryApi.create(form);showToast(`Category ${editing?'updated':'created'} successfully`);setOpen(false);setEditing(null);setForm(blank);await load()}catch(err){showToast(getApiErrorMessage(err),'error')}finally{setBusy(false)}}
  const remove=async()=>{if(!deleting)return;setBusy(true);try{await categoryApi.remove(deleting.id);showToast('Category deleted successfully');setDeleting(null);await load()}catch(e){showToast(getApiErrorMessage(e),'error')}finally{setBusy(false)}}
  return <><PageHeader eyebrow="Organization" title="Categories" description="Separate income and expenses into clear categories for reports and budgets." action={<button className="button-primary" onClick={()=>{setEditing(null);setForm(blank);setOpen(true)}}><Plus size={17}/>Add category</button>}/>
  {loading?<LoadingState/>:error?<ErrorState message={error} onRetry={load}/>:items.length===0?<EmptyState title="No categories yet" description="Add an income or expense category to continue."/>:<div className="grid gap-6 lg:grid-cols-2">{(['EXPENSE','INCOME'] as TransactionType[]).map(type=><section className="panel overflow-hidden" key={type}><div className="flex items-center gap-3 border-b border-slate-100 p-5"><span className={`stat-icon ${type==='INCOME'?'stat-icon-emerald':'stat-icon-amber'}`}><Tags size={18}/></span><div><h2 className="font-bold text-slate-900">{type==='INCOME'?'Income':'Expense'} categories</h2><p className="text-xs text-slate-400">{items.filter(x=>x.type===type).length} total</p></div></div><div className="divide-y divide-slate-100">{items.filter(x=>x.type===type).map(c=><div className="flex items-center justify-between gap-3 p-4" key={c.id}><div><p className="font-semibold text-slate-800">{c.name}</p><p className="mt-1 text-sm text-slate-400">{c.description||'No description'}</p></div><div className="flex gap-2"><button className="icon-button" onClick={()=>edit(c)}><Pencil size={16}/></button><button className="icon-button" onClick={()=>setDeleting(c)}><Trash2 size={16}/></button></div></div>)}</div></section>)}</div>}
  <Modal open={open} title={editing?'Edit category':'New category'} onClose={()=>setOpen(false)}><form className="space-y-4" onSubmit={submit}><label><span className="field-label">Name</span><input className="field-input" required maxLength={100} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label><span className="field-label">Type</span><select className="field-input" value={form.type} onChange={e=>setForm({...form,type:e.target.value as TransactionType})}><option value="EXPENSE">Expense</option><option value="INCOME">Income</option></select></label><label><span className="field-label">Description</span><textarea className="field-textarea" rows={3} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label><div className="flex justify-end gap-3"><button type="button" className="button-secondary" onClick={()=>setOpen(false)}>Cancel</button><button className="button-primary" disabled={busy}>{busy?'Saving…':'Save category'}</button></div></form></Modal>
  <ConfirmDialog open={!!deleting} title="Delete category?" message="The backend may reject this if the category is still used by transactions or budgets." busy={busy} onClose={()=>setDeleting(null)} onConfirm={remove}/></>
}

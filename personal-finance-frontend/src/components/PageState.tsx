import { CircleAlert, Inbox, LoaderCircle, RefreshCw } from 'lucide-react'

export function LoadingState({ label = 'Loading your data…' }: { label?: string }) {
  return <div className="page-state"><LoaderCircle className="animate-spin text-emerald-600" size={28} /><p>{label}</p></div>
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return <div className="page-state"><Inbox className="text-slate-400" size={30} /><h3>{title}</h3><p>{description}</p></div>
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="page-state border-rose-100 bg-rose-50/50">
      <CircleAlert className="text-rose-500" size={30} />
      <h3>We couldn't load this page</h3><p>{message}</p>
      {onRetry && <button className="button-secondary mt-2" onClick={onRetry}><RefreshCw size={16} /> Try again</button>}
    </div>
  )
}

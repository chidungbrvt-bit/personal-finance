import type { LucideIcon } from 'lucide-react'

export function StatCard({ label, value, helper, icon: Icon, tone = 'emerald' }: {
  label: string; value: string; helper: string; icon: LucideIcon; tone?: 'emerald' | 'blue' | 'amber' | 'slate'
}) {
  return (
    <article className="panel p-5 sm:p-6">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-slate-500">{label}</p>
        <span className={`stat-icon stat-icon-${tone}`}><Icon size={19} /></span>
      </div>
      <p className="mt-5 truncate text-2xl font-bold tracking-tight text-slate-950">{value}</p>
      <p className="mt-2 text-xs font-medium text-slate-400">{helper}</p>
    </article>
  )
}

import { Landmark, LayoutDashboard, LogOut, Menu, Tags, Target, WalletCards, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const navigation = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/accounts', label: 'Accounts', icon: WalletCards },
  { to: '/categories', label: 'Categories', icon: Tags },
  { to: '/transactions', label: 'Transactions', icon: Landmark },
  { to: '/budgets', label: 'Budgets', icon: Target },
]

export function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { session, logout } = useAuth()

  const sidebar = (
    <>
      <div className="flex items-center gap-3 px-3 py-2">
        <div className="grid size-10 place-items-center rounded-2xl bg-emerald-600 text-white shadow-lg shadow-emerald-950/20"><WalletCards size={21} /></div>
        <div><p className="text-lg font-bold tracking-tight text-white">Evergreen</p><p className="text-xs text-slate-400">Personal finance</p></div>
      </div>
      <nav className="mt-9 space-y-1.5">
        {navigation.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to} onClick={() => setMenuOpen(false)} className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>
            <Icon size={19} /><span>{label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="mt-auto rounded-2xl border border-slate-700/70 bg-slate-800/70 p-4">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Signed in</p>
        <p className="mt-2 truncate text-sm font-semibold text-white">{session?.user.name}</p>
        <p className="mt-1 truncate text-xs text-slate-400">{session?.user.email}</p>
        <button className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-slate-700 px-3 py-2 text-sm font-semibold text-slate-300 transition hover:bg-slate-700 hover:text-white" onClick={logout}>
          <LogOut size={16} /> Sign out
        </button>
      </div>
    </>
  )

  return (
    <div className="min-h-screen bg-[#f6f8f7]">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col bg-slate-950 px-4 py-6 lg:flex">{sidebar}</aside>
      {menuOpen && <div className="fixed inset-0 z-40 bg-slate-950/50 backdrop-blur-sm lg:hidden" onClick={() => setMenuOpen(false)} />}
      <aside className={`fixed inset-y-0 left-0 z-50 flex w-72 flex-col bg-slate-950 px-4 py-5 transition-transform lg:hidden ${menuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <button className="icon-button ml-auto !border-slate-700 !text-white" onClick={() => setMenuOpen(false)} aria-label="Close menu"><X size={20} /></button>
        {sidebar}
      </aside>
      <div className="lg:pl-64">
        <div className="sticky top-0 z-20 flex h-16 items-center border-b border-slate-200/80 bg-white/90 px-4 backdrop-blur lg:hidden">
          <button className="icon-button" onClick={() => setMenuOpen(true)} aria-label="Open menu"><Menu size={21} /></button>
          <p className="ml-3 font-bold text-slate-900">Evergreen Finance</p>
        </div>
        <main className="mx-auto min-h-screen max-w-[1500px] px-4 py-7 sm:px-7 sm:py-9 xl:px-10"><Outlet /></main>
      </div>
    </div>
  )
}

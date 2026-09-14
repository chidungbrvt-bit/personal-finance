import { Languages } from 'lucide-react'
import { useLanguage } from '../i18n/LanguageContext'

export function LanguageToggle() {
  const { language, toggleLanguage } = useLanguage()
  const vietnamese = language === 'vi'

  return (
    <button
      type="button"
      onClick={toggleLanguage}
      className="fixed right-4 top-4 z-[70] flex items-center gap-2 rounded-full border border-slate-200 bg-white/95 px-2.5 py-2 text-xs font-bold text-slate-700 shadow-lg backdrop-blur transition hover:border-emerald-300 sm:right-6 sm:top-5"
      aria-label={vietnamese ? 'Switch to English' : 'Chuyển sang tiếng Việt'}
      title={vietnamese ? 'Switch to English' : 'Chuyển sang tiếng Việt'}
    >
      <Languages size={17} />
      <span className="text-slate-400">EN</span>
      <span className={`relative h-6 w-11 rounded-full transition ${vietnamese ? 'bg-emerald-600' : 'bg-slate-300'}`}>
        <span className={`absolute left-0 top-1 size-4 rounded-full bg-white shadow transition-transform duration-200 ${vietnamese ? 'translate-x-1' : 'translate-x-6'}`} />
      </span>
      <span className="text-slate-400">VI</span>
    </button>
  )
}

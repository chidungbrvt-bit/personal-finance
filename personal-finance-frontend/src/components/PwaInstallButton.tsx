import { Download } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Modal } from './Modal'

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed'; platform: string }>
}

export function PwaInstallButton() {
  const [installPrompt, setInstallPrompt] = useState<BeforeInstallPromptEvent | null>(null)
  const [showIosHelp, setShowIosHelp] = useState(false)
  const userAgent = navigator.userAgent.toLowerCase()
  const isIos = /iphone|ipad|ipod/.test(userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)
  const isStandalone = window.matchMedia('(display-mode: standalone)').matches || Boolean((navigator as Navigator & { standalone?: boolean }).standalone)

  useEffect(() => {
    const capturePrompt = (event: Event) => {
      event.preventDefault()
      setInstallPrompt(event as BeforeInstallPromptEvent)
    }
    const installed = () => setInstallPrompt(null)
    window.addEventListener('beforeinstallprompt', capturePrompt)
    window.addEventListener('appinstalled', installed)
    return () => {
      window.removeEventListener('beforeinstallprompt', capturePrompt)
      window.removeEventListener('appinstalled', installed)
    }
  }, [])

  const install = async () => {
    if (!installPrompt) return
    await installPrompt.prompt()
    await installPrompt.userChoice
    setInstallPrompt(null)
  }

  if (!installPrompt && (!isIos || isStandalone)) return null

  return <>
    <button type="button" className="button-primary fixed bottom-4 right-4 z-[60] shadow-xl sm:bottom-6 sm:right-6" onClick={isIos ? () => setShowIosHelp(true) : install}><Download size={17}/>Install app</button>
    <Modal open={showIosHelp} title="Install on iPhone or iPad" onClose={() => setShowIosHelp(false)}>
      <ol className="list-decimal space-y-3 pl-5 text-sm leading-6 text-slate-600">
        <li>Open this website in Safari.</li>
        <li>Tap the Share button at the bottom of Safari.</li>
        <li>Scroll down and choose Add to Home Screen.</li>
        <li>Tap Add. Personal Finance will appear on your Home Screen.</li>
      </ol>
    </Modal>
  </>
}

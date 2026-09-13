export const appConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL
    || 'https://personal-finance-backend-production-51f2.up.railway.app',
} as const

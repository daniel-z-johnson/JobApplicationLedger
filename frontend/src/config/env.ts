const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

if (!apiBaseUrl || !apiBaseUrl.startsWith('/') || apiBaseUrl.startsWith('//') || /[?#\s]/.test(apiBaseUrl)) {
  throw new Error('VITE_API_BASE_URL must be a same-origin path such as /api.')
}

export const env = {
  apiBaseUrl: apiBaseUrl.replace(/\/+$/, ''),
} as const

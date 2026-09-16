import { apiRequest } from '../../../api/request'

// Restore authentication without storing profile data in the auth feature.
export async function checkSession(signal?: AbortSignal): Promise<boolean> {
  const response = await apiRequest('/u/me', { signal })
  if (response.status === 401) return false
  if (!response.ok) throw new Error('Unable to check your session. Please try again.')
  return true
}

async function postSession(action: 'login' | 'logout', details?: { email: string; password: string }) {
  const response = await apiRequest(`/u/${action}`, { method: 'POST', json: details })
  if (action === 'logout' && response.status === 401) return
  if (response.status === 401) throw new Error('Email or password is incorrect.')
  if (response.status === 403) throw new Error('Your security token expired. Please try again.')
  if (!response.ok) throw new Error(`Unable to ${action === 'login' ? 'log in' : 'log out'}. Please try again.`)
}

export async function login(details: { email: string; password: string }) {
  await postSession('login', details)
}

export async function logout() {
  await postSession('logout')
}

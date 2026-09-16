import { env } from '../../../config/env'
import { getCsrfToken } from '../../../api/csrf'

// Restore authentication without storing profile data in the auth feature.
export async function checkSession(signal?: AbortSignal): Promise<boolean> {
  const response = await fetch(`${env.apiBaseUrl}/u/me`, { credentials: 'same-origin', cache: 'no-store', signal })
  if (response.status === 401) return false
  if (!response.ok) throw new Error('Unable to check your session. Please try again.')
  return true
}

async function postSession(action: 'login' | 'logout', details?: { email: string; password: string }) {
  const token = await getCsrfToken()
  const response = await fetch(`${env.apiBaseUrl}/u/${action}`, {
    method: 'POST', credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': token },
    body: details ? JSON.stringify(details) : undefined,
  })
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

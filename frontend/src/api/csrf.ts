import { env } from '../config/env'

export class CsrfError extends Error {}

export async function getCsrfToken(signal?: AbortSignal): Promise<string> {
  // Authentication changes can rotate the token, so refresh it for each mutation.
  const response = await fetch(`${env.apiBaseUrl}/u/csrf`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })
  if (!response.ok) throw new CsrfError('Unable to prepare a secure request. Please try again.')

  // Spring's SPA handler expects the raw cookie token, not the masked JSON token.
  const cookie = document.cookie.split(';').map(value => value.trim()).find(value => value.startsWith('XSRF-TOKEN='))
  const token = cookie ? decodeURIComponent(cookie.slice('XSRF-TOKEN='.length)) : ''
  if (!token) throw new CsrfError('Unable to read the security cookie. Enable cookies and try again.')
  return token
}

import { env } from '../config/env'
import { getCsrfToken } from './csrf'

type RequestOptions = {
  method?: 'GET' | 'HEAD' | 'OPTIONS' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  json?: unknown
  signal?: AbortSignal
}

// Return the response untouched: each feature owns its response and error contract.
export async function apiRequest(path: `/${string}`, { method = 'GET', json, signal }: RequestOptions = {}): Promise<Response> {
  signal?.throwIfAborted()
  const headers: Record<string, string> = {}
  const body = json === undefined ? undefined : JSON.stringify(json)
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    headers['X-XSRF-TOKEN'] = await getCsrfToken(signal)
  }
  signal?.throwIfAborted()

  // Do not retry mutations: the server may already have applied the operation.
  return fetch(`${env.apiBaseUrl}${path}`, {
    method,
    credentials: 'same-origin',
    cache: 'no-store',
    headers,
    body,
    signal,
  })
}

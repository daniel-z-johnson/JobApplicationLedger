import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { apiRequest } from './request'

const fetchMock = vi.fn<typeof fetch>()
beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock)
  vi.stubGlobal('document', { cookie: 'XSRF-TOKEN=raw-token' })
})
afterEach(() => { vi.unstubAllGlobals(); vi.resetAllMocks() })

describe('apiRequest', () => {
  it.each(['GET', 'HEAD', 'OPTIONS'] as const)('sends %s without a CSRF round trip', async method => {
    const response = new Response(null, { status: 204 })
    fetchMock.mockResolvedValueOnce(response)
    await expect(apiRequest('/example', { method })).resolves.toBe(response)
    expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/example', expect.objectContaining({
      method, credentials: 'same-origin', cache: 'no-store', headers: {}, body: undefined,
    }))
  })

  it.each(['POST', 'PUT', 'PATCH', 'DELETE'] as const)('secures %s with a refreshed token and forwards cancellation', async method => {
    const controller = new AbortController()
    fetchMock.mockResolvedValueOnce(Response.json({ token: 'masked-token' }))
      .mockResolvedValueOnce(new Response(null, { status: 204 }))
    await apiRequest('/example', { method, json: { value: 1 }, signal: controller.signal })
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/u/csrf', expect.objectContaining({ signal: controller.signal }))
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/example', expect.objectContaining({
      method, credentials: 'same-origin', signal: controller.signal, body: '{"value":1}',
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'raw-token' },
    }))
  })

  it('returns HTTP errors unchanged without retrying a mutation', async () => {
    const response = Response.json({ message: 'Rejected' }, { status: 403 })
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(response)
    await expect(apiRequest('/example', { method: 'POST' })).resolves.toBe(response)
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })

  it('does not submit a mutation when canceled during CSRF preparation', async () => {
    const controller = new AbortController()
    fetchMock.mockImplementationOnce(async () => {
      controller.abort()
      return Response.json({})
    })
    await expect(apiRequest('/example', { method: 'POST', signal: controller.signal })).rejects.toMatchObject({ name: 'AbortError' })
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('does not retry a mutation when the connection fails', async () => {
    fetchMock.mockResolvedValueOnce(Response.json({})).mockRejectedValueOnce(new TypeError('Failed to fetch'))
    await expect(apiRequest('/example', { method: 'POST' })).rejects.toThrow('Failed to fetch')
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })
})

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { getProfile } from './profile'

const fetchMock = vi.fn<typeof fetch>()
beforeEach(() => { vi.stubGlobal('fetch', fetchMock) })
afterEach(() => { vi.unstubAllGlobals(); vi.resetAllMocks() })

describe('profile', () => {
  it('loads only the username into profile state without caching the response', async () => {
    fetchMock.mockResolvedValueOnce(Response.json({ username: 'test_user', email: 'test@example.com', recentLogins: [] }))
    await expect(getProfile()).resolves.toEqual({ username: 'test_user' })
    expect(fetchMock).toHaveBeenCalledWith('/api/u/me', expect.objectContaining({ credentials: 'same-origin', cache: 'no-store' }))
  })

  it('treats an expired session as signed out', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 401 }))
    await expect(getProfile()).resolves.toBeNull()
  })

  it('does not confuse a server failure with a signed-out session', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 503 }))
    await expect(getProfile()).rejects.toThrow('Unable to load')
  })

})

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { getProfile, login, logout } from './session'

const fetchMock = vi.fn<typeof fetch>()
beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock)
  vi.stubGlobal('document', { cookie: 'XSRF-TOKEN=raw-token' })
})
afterEach(() => { vi.unstubAllGlobals(); vi.resetAllMocks() })

describe('session', () => {
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

  it('posts login with credentials and the raw CSRF cookie', async () => {
    const details = { email: 'test@example.com', password: 'password' }
    fetchMock.mockResolvedValueOnce(Response.json({ token: 'masked-token' })).mockResolvedValueOnce(Response.json({ username: 'test_user' }))
    await login(details)
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/u/login', expect.objectContaining({
      method: 'POST', credentials: 'same-origin', body: JSON.stringify(details),
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'raw-token' },
    }))
  })

  it('reports rejected credentials', async () => {
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(new Response(null, { status: 401 }))
    await expect(login({ email: 'test@example.com', password: 'wrong' })).rejects.toThrow('Email or password is incorrect')
  })

  it('refreshes CSRF before logout after login has rotated the token', async () => {
    fetchMock.mockImplementationOnce(async () => {
      document.cookie = 'XSRF-TOKEN=new-token'
      return Response.json({})
    }).mockResolvedValueOnce(new Response(null, { status: 204 }))
    await logout()
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/u/csrf', expect.objectContaining({ cache: 'no-store' }))
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/u/logout', expect.objectContaining({
      method: 'POST', credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'new-token' },
    }))
  })

  it('keeps logout failures visible so the UI does not claim the session ended', async () => {
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(new Response(null, { status: 500 }))
    await expect(logout()).rejects.toThrow('Unable to log out')
  })

  it('does not submit credentials without a security cookie', async () => {
    document.cookie = ''
    fetchMock.mockResolvedValueOnce(Response.json({}))
    await expect(login({ email: 'test@example.com', password: 'password' })).rejects.toThrow('Enable cookies')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
})

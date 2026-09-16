import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { signup } from './signup'

const details = { email: 'test@example.com', username: 'test_user', password: 'sample-password', confirmPassword: 'sample-password' }
const fetchMock = vi.fn<typeof fetch>()

beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock)
  vi.stubGlobal('document', { cookie: '' })
})
afterEach(() => { vi.unstubAllGlobals(); vi.resetAllMocks() })

describe('signup', () => {
  it('waits for CSRF, then submits JSON with the raw cookie token and credentials', async () => {
    fetchMock.mockImplementationOnce(async () => {
      expect(fetchMock).toHaveBeenCalledTimes(1)
      document.cookie = 'XSRF-TOKEN=raw-token'
      return Response.json({ token: 'masked-token' })
    }).mockResolvedValueOnce(Response.json({ id: 1 }))
    await signup(details)
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/u/csrf', expect.objectContaining({ credentials: 'same-origin', cache: 'no-store' }))
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/u/register', expect.objectContaining({
      method: 'POST', credentials: 'same-origin', body: JSON.stringify(details),
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'raw-token' },
    }))
  })

  it('does not post when fetching CSRF fails', async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 503 }))
    await expect(signup(details)).rejects.toThrow('Unable to prepare')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('does not post when the cookie is missing', async () => {
    fetchMock.mockResolvedValueOnce(Response.json({ token: 'masked-token' }))
    await expect(signup(details)).rejects.toThrow('security cookie')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('preserves backend validation errors', async () => {
    document.cookie = 'XSRF-TOKEN=raw-token'
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(Response.json({
      message: 'Request validation failed', violations: { username: ['Invalid username'] },
    }, { status: 400 }))
    await expect(signup(details)).rejects.toMatchObject({ violations: { username: ['Invalid username'] } })
  })

  it('reports duplicate accounts', async () => {
    document.cookie = 'XSRF-TOKEN=raw-token'
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(Response.json({ message: 'Email already exists' }, { status: 409 }))
    await expect(signup(details)).rejects.toThrow('Email already exists')
  })

  it('does not automatically retry a rejected registration', async () => {
    document.cookie = 'XSRF-TOKEN=raw-token'
    fetchMock.mockResolvedValueOnce(Response.json({})).mockResolvedValueOnce(new Response(null, { status: 403 }))
    await expect(signup(details)).rejects.toThrow('fresh token')
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })
})

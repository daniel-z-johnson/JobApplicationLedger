import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiRequest } from '../../../api/request'
import { createCompany, getCompany, listCompanies } from './companies'

vi.mock('../../../api/request', () => ({ apiRequest: vi.fn() }))
const request = vi.mocked(apiRequest)
afterEach(() => vi.resetAllMocks())

describe('companies API', () => {
  it('loads company details and forwards the cancellation signal', async () => {
    const company = { id: 'company-1', name: 'Acme', companyType: 'Financial services', websiteUrl: 'https://example.com', careersUrl: null, createdAt: '2026-09-18T12:00:00Z' }
    const controller = new AbortController()
    request.mockResolvedValueOnce(Response.json(company))
    await expect(getCompany('company-1', controller.signal)).resolves.toEqual(company)
    expect(request).toHaveBeenCalledWith('/companies/company-1', { signal: controller.signal })
  })

  it('distinguishes a missing company from session expiration and server failures', async () => {
    for (const status of [404, 401, 503]) {
      request.mockResolvedValueOnce(new Response(null, { status }))
      await expect(getCompany('missing')).rejects.toMatchObject({ status })
    }
  })

  it('requests alphabetical server pagination with 5 companies per page', async () => {
    const page = { content: [], page: { number: 2, size: 5, totalElements: 0, totalPages: 0 } }
    request.mockResolvedValueOnce(Response.json(page))
    await expect(listCompanies(2)).resolves.toEqual(page)
    expect(request).toHaveBeenCalledWith('/companies?page=2&size=5&sort=name,asc', { signal: undefined })
  })

  it('keeps session expiration distinguishable from load failure', async () => {
    request.mockResolvedValueOnce(new Response(null, { status: 401 }))
    await expect(listCompanies(0)).rejects.toMatchObject({ status: 401 })
    request.mockResolvedValueOnce(new Response(null, { status: 503 }))
    await expect(listCompanies(0)).rejects.toMatchObject({ status: 503 })
  })

  it('creates through the shared secure request helper without ownership fields', async () => {
    const details = { name: 'Acme', companyType: 'Financial services', websiteUrl: null, careersUrl: null }
    const created = { ...details, id: 'new-company', createdAt: '2026-09-18T12:00:00Z', updatedAt: '2026-09-18T12:00:00Z' }
    request.mockResolvedValueOnce(Response.json(created, { status: 201 }))
    await expect(createCompany(details)).resolves.toEqual(created)
    expect(request).toHaveBeenCalledWith('/companies', { method: 'POST', json: details, signal: undefined })
  })

  it('reports duplicate names without retrying the write', async () => {
    request.mockResolvedValueOnce(new Response(null, { status: 409 }))
    await expect(createCompany({ name: 'Acme', companyType: 'Financial services', websiteUrl: null, careersUrl: null })).rejects.toMatchObject({ status: 409, message: 'A company with this name already exists.' })
    expect(request).toHaveBeenCalledTimes(1)
  })
})

import { apiRequest } from '../../../api/request'

export type Company = { id: string; name: string; companyType: string; createdAt: string }
export type CompanyDetails = Company & { websiteUrl: string | null; careersUrl: string | null; updatedAt: string }
export type CompanyPage = { content: Company[]; page: { number: number; totalPages: number; totalElements: number; size: number } }
export type NewCompany = { name: string; companyType: string; websiteUrl: string | null; careersUrl: string | null }

export class CompanyError extends Error {
  status: number
  constructor(message: string, status = 0) {
    super(message)
    this.status = status
  }
}

export async function listCompanies(page: number, signal?: AbortSignal): Promise<CompanyPage> {
  const response = await apiRequest(`/companies?page=${page}&size=5&sort=name,asc`, { signal })
  if (!response.ok) throw new CompanyError('Unable to load companies. Please try again.', response.status)
  return response.json()
}

export async function getCompany(id: string, signal?: AbortSignal): Promise<CompanyDetails> {
  const response = await apiRequest(`/companies/${encodeURIComponent(id)}`, { signal })
  if (!response.ok) throw new CompanyError(response.status === 404 ? 'Company not found.' : 'Unable to load company. Please try again.', response.status)
  return response.json()
}

export async function createCompany(details: NewCompany, signal?: AbortSignal): Promise<void> {
  const response = await apiRequest('/companies', { method: 'POST', json: details, signal })
  if (response.ok) return
  if (response.status === 409) throw new CompanyError('A company with this name already exists.', 409)
  if (response.status === 403) throw new CompanyError('Your security token could not be verified. Please submit again.', 403)
  if (response.status === 400) throw new CompanyError('Check the company details. URLs must be valid HTTP or HTTPS addresses.', 400)
  throw new CompanyError('Unable to add the company. Please try again.', response.status)
}

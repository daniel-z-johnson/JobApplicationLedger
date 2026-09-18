import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router'
import { CompanyError, getCompany } from './api/companies'
import type { CompanyDetails } from './api/companies'
import { formatDateTime } from './formatDateTime'

export default function CompanyDetailPage({ onSessionExpired }: { onSessionExpired: () => void }) {
  const { companyId = '' } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [attempt, setAttempt] = useState(0)
  return <main className="mx-auto max-w-4xl px-4 py-10 sm:py-16">
    <Link to="/companies" className="mb-6 inline-block text-sm underline">Back to companies</Link>
    {location.state?.createdCompanyId === companyId && <div className="mb-6 flex items-center justify-between gap-4 rounded-lg border border-green-700 bg-green-950 p-4 text-green-100">
      <p role="status">Company added.</p>
      <button type="button" onClick={() => navigate(location.pathname, { replace: true, state: null })} className="text-sm underline">Dismiss</button>
    </div>}
    <CompanyDetail key={`${companyId}-${attempt}`} id={companyId} onRetry={() => setAttempt(value => value + 1)} onSessionExpired={onSessionExpired} />
  </main>
}

function CompanyDetail({ id, onRetry, onSessionExpired }: { id: string; onRetry: () => void; onSessionExpired: () => void }) {
  const [company, setCompany] = useState<CompanyDetails | null>(null)
  const [error, setError] = useState<CompanyError | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    getCompany(id, controller.signal).then(value => {
      if (!controller.signal.aborted) setCompany(value)
    }).catch(failure => {
      if (controller.signal.aborted) return
      if (failure instanceof CompanyError && failure.status === 401) onSessionExpired()
      else setError(failure instanceof CompanyError ? failure : new CompanyError('Unable to load company. Please try again.'))
    })
    return () => controller.abort()
  }, [id, onSessionExpired])

  if (error) return <div role="alert" className="rounded-md border border-red-400 bg-red-950 p-4 text-red-100">
    {error.message} {error.status !== 404 && <button onClick={onRetry} className="underline">Try again</button>}
  </div>
  if (!company) return <p role="status">Loading company…</p>
  return <>
    <section aria-labelledby="company-heading" className="overflow-hidden rounded-xl border border-gray-700 bg-gray-800">
      <div className="p-5 sm:p-8">
        <h1 id="company-heading" className="break-words text-3xl font-bold tracking-tight sm:text-4xl">{company.name}</h1>
        <dl className="mt-8 grid grid-cols-1 gap-4 lg:grid-cols-2">
          <div className="min-w-0">
            <dt className="text-[10px] font-semibold text-gray-400">Website URL</dt>
            <dd className="mt-2 break-words text-sm leading-6 text-gray-100"><CompanyUrl value={company.websiteUrl} /></dd>
          </div>
          <div className="min-w-0">
            <dt className="text-[10px] font-semibold text-gray-400">Careers URL</dt>
            <dd className="mt-2 break-words text-sm leading-6 text-gray-100"><CompanyUrl value={company.careersUrl} /></dd>
          </div>
        </dl>
      </div>
      <dl className="grid grid-cols-1 gap-5 border-t border-gray-700 bg-gray-900/30 px-5 py-5 sm:px-8 lg:grid-cols-2">
        <div className="min-w-0">
          <dt className="text-[10px] font-semibold text-gray-400">Created at</dt>
          <dd className="mt-2 text-sm text-gray-300"><time dateTime={company.createdAt}>{formatDateTime(company.createdAt)}</time></dd>
        </div>
        <div className="min-w-0">
          <dt className="text-[10px] font-semibold text-gray-400">Updated at</dt>
          <dd className="mt-2 text-sm text-gray-300"><time dateTime={company.updatedAt}>{formatDateTime(company.updatedAt)}</time></dd>
        </div>
      </dl>
    </section>
    <section aria-labelledby="applications-heading" className="mt-6 rounded-xl border border-gray-700 bg-gray-800/40 p-5 sm:p-8">
      <h2 id="applications-heading" className="text-xl font-semibold">Applications</h2>
      <div className="mt-5 rounded-lg border border-dashed border-gray-600 px-4 py-8 text-center">
        <p className="text-sm text-gray-300">Applications for this company will be listed here.</p>
        <p className="mt-2 text-xs text-gray-400">Coming soon</p>
      </div>
    </section>
  </>
}

function CompanyUrl({ value }: { value: string | null }) {
  if (!value) return <span className="text-gray-400">Not provided</span>
  let href: string | undefined
  try {
    const url = new URL(value)
    if (url.protocol === 'http:' || url.protocol === 'https:') {
      href = url.href
    }
  } catch { /* Display invalid URLs as text instead of creating a link. */ }
  if (href) return <a href={href} target="_blank" rel="noopener noreferrer" className="underline hover:text-gray-300">{value}<span className="sr-only"> (opens in a new tab)</span></a>
  return <span>{value}</span>
}

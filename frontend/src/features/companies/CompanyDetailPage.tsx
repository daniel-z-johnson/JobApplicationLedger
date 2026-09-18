import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { CompanyError, getCompany } from './api/companies'
import type { CompanyDetails } from './api/companies'
import { formatDateTime } from './formatDateTime'

export default function CompanyDetailPage({ onSessionExpired }: { onSessionExpired: () => void }) {
  const { companyId = '' } = useParams()
  const [attempt, setAttempt] = useState(0)
  return <main className="mx-auto max-w-4xl px-4 py-10 sm:py-16">
    <Link to="/companies" className="mb-6 inline-block text-sm underline">Back to companies</Link>
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
    <section aria-labelledby="company-heading" className="rounded-xl border border-gray-600 bg-gray-800 p-6 sm:p-8">
      <h1 id="company-heading" className="break-words text-2xl font-bold">{company.name}</h1>
      <dl className="mt-6 grid grid-cols-1 gap-5 lg:grid-cols-2">
        <div className="min-w-0"><dt className="mb-2 text-xs font-semibold text-gray-300">Company type</dt><dd className="rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 break-words text-gray-100">{company.companyType}</dd></div>
        <div className="min-w-0"><dt className="mb-2 text-xs font-semibold text-gray-300">Website URL</dt><dd className="rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 break-words text-gray-100"><CompanyUrl value={company.websiteUrl} /></dd></div>
        <div className="min-w-0"><dt className="mb-2 text-xs font-semibold text-gray-300">Careers URL</dt><dd className="rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 break-words text-gray-100"><CompanyUrl value={company.careersUrl} /></dd></div>
        <div className="min-w-0"><dt className="mb-2 text-xs font-semibold text-gray-300">Created at</dt><dd className="rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 break-words text-gray-100"><time dateTime={company.createdAt}>{formatDateTime(company.createdAt)}</time></dd></div>
        <div className="min-w-0"><dt className="mb-2 text-xs font-semibold text-gray-300">Updated at</dt><dd className="rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 break-words text-gray-100"><time dateTime={company.updatedAt}>{formatDateTime(company.updatedAt)}</time></dd></div>
      </dl>
    </section>
    <section aria-labelledby="applications-heading" className="mt-8 rounded-xl border border-dashed border-gray-600 p-6 sm:p-8">
      <h2 id="applications-heading" className="text-xl font-semibold">Applications</h2>
      <p className="mt-3 text-gray-300">Applications for this company will be listed here. This feature is coming soon.</p>
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

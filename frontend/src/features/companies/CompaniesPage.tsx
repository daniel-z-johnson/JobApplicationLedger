import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { CompanyError, listCompanies } from './api/companies'
import type { CompanyPage } from './api/companies'

import { formatDateTime } from './formatDateTime'

export default function CompaniesPage({ onSessionExpired }: { onSessionExpired: () => void }) {
  const [params, setParams] = useSearchParams()
  const requestedPage = Number(params.get('page') ?? 0)
  const page = Number.isSafeInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0
  const [attempt, setAttempt] = useState(0)
  return <main className="mx-auto max-w-4xl px-4 py-10 sm:py-16">
    <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
      <div><h1 className="text-2xl font-bold">Companies</h1></div>
      <Link to="/companies/new" className="rounded-md bg-gray-100 px-4 py-3 font-semibold text-gray-900 hover:bg-gray-300">Add company</Link>
    </div>
    <CompanyList key={`${page}-${attempt}`} page={page} onPage={next => setParams({ page: String(next) })} onRetry={() => setAttempt(value => value + 1)} onSessionExpired={onSessionExpired} />
  </main>
}

function CompanyList({ page, onPage, onRetry, onSessionExpired }: { page: number; onPage: (page: number) => void; onRetry: () => void; onSessionExpired: () => void }) {
  const [result, setResult] = useState<CompanyPage | null>(null)
  const [error, setError] = useState(false)
  useEffect(() => {
    const controller = new AbortController()
    listCompanies(page, controller.signal).then(data => {
      if (!controller.signal.aborted) setResult(data)
    }).catch(failure => {
      if (controller.signal.aborted) return
      if (failure instanceof CompanyError && failure.status === 401) onSessionExpired()
      else setError(true)
    })
    return () => controller.abort()
  }, [page, onSessionExpired])

  if (error) return <div role="alert" className="rounded-md border border-red-400 bg-red-950 p-4 text-red-100">Unable to load companies. <button onClick={onRetry} className="underline">Try again</button></div>
  if (!result) return <p role="status">Loading companies…</p>
  if (!result.page.totalElements) return <p className="rounded-xl border border-gray-600 bg-gray-800 p-8">You haven’t added any companies yet.</p>
  return <>
    <p role="status" className="mb-4 text-sm text-gray-300">
      {result.content.length
        ? `Showing ${result.page.number * result.page.size + 1}–${result.page.number * result.page.size + result.content.length} of ${result.page.totalElements} ${result.page.totalElements === 1 ? 'company' : 'companies'}`
        : `Showing 0 of ${result.page.totalElements} companies`}
    </p>
    <CompanyPagination page={page} totalPages={result.page.totalPages} onPage={onPage} />
    {result.content.length ? <ul className="divide-y divide-gray-600 overflow-hidden rounded-xl border border-gray-600 bg-gray-800">
      {result.content.map(company => <li key={company.id}>
        <Link to={`/companies/${company.id}`} className="block p-5 transition-colors hover:bg-gray-700 focus-visible:bg-gray-700 focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-gray-300">
        <h2 className="break-words text-lg font-semibold">{company.name}</h2>
        <p className="mt-1 break-words text-sm text-gray-300">{company.companyType}</p>
        <p className="mt-2 text-sm text-gray-300">Added: <time dateTime={company.createdAt}>{formatDateTime(company.createdAt)}</time></p>
        </Link>
      </li>)}
    </ul> : <p>No companies on this page. <button className="underline" onClick={() => onPage(0)}>Return to the first page</button></p>}
    <CompanyPagination page={page} totalPages={result.page.totalPages} onPage={onPage} />
  </>
}

function CompanyPagination({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) {
  return <nav aria-label="Company pages" className="my-6 flex flex-wrap items-center justify-between gap-4">
      <button disabled={page === 0} onClick={() => onPage(page - 1)} className="rounded-md border border-gray-500 px-4 py-2 hover:bg-gray-700 disabled:opacity-40">Previous</button>
      <p className="text-sm text-gray-300">{page < totalPages ? `Page ${page + 1} of ${totalPages}` : 'Page unavailable'}</p>
      <button disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)} className="rounded-md border border-gray-500 px-4 py-2 hover:bg-gray-700 disabled:opacity-40">Next</button>
    </nav>
}

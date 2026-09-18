import { useEffect, useRef, useState } from 'react'
import type { SubmitEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { CompanyError, createCompany } from './api/companies'

const fields = [
  { name: 'name', label: 'Company name', required: true, maxLength: 255, type: 'text' },
  { name: 'companyType', label: 'Company type', required: true, maxLength: 127, type: 'text' },
  { name: 'websiteUrl', label: 'Website URL (optional)', required: false, type: 'url' },
  { name: 'careersUrl', label: 'Careers URL (optional)', required: false, type: 'url' },
] as const

export default function AddCompanyPage({ onSessionExpired }: { onSessionExpired: () => void }) {
  const navigate = useNavigate()
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const request = useRef<AbortController | null>(null)
  const feedback = useRef<HTMLParagraphElement>(null)
  useEffect(() => () => request.current?.abort(), [])
  useEffect(() => { if (error) feedback.current?.focus() }, [error])

  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (request.current) return
    const data = new FormData(event.currentTarget)
    const value = (name: string) => String(data.get(name) ?? '').trim()
    if (!value('name') || !value('companyType')) { setError('Enter a company name and type.'); return }
    const controller = new AbortController()
    request.current = controller
    setPending(true)
    setError('')
    try {
      await createCompany({ name: value('name'), companyType: value('companyType'), websiteUrl: value('websiteUrl') || null, careersUrl: value('careersUrl') || null }, controller.signal)
      if (!controller.signal.aborted) navigate('/companies', { replace: true })
    } catch (failure) {
      if (controller.signal.aborted) return
      if (failure instanceof CompanyError && failure.status === 401) onSessionExpired()
      else setError(failure instanceof Error ? failure.message : 'Unable to add the company. Please try again.')
    } finally {
      request.current = null
      if (!controller.signal.aborted) setPending(false)
    }
  }

  return <main className="mx-auto max-w-lg px-4 py-10 sm:py-16">
    <h1 className="mb-6 text-2xl font-bold">Add company</h1>
    <form onSubmit={submit} aria-busy={pending} className="rounded-xl border border-gray-600 bg-gray-800 p-6">
      {error && <p ref={feedback} tabIndex={-1} role="alert" className="mb-5 rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{error}</p>}
      <fieldset disabled={pending} className="space-y-5 disabled:opacity-70">
        {fields.map(field => <div key={field.name}>
          <label htmlFor={field.name} className="mb-2 block text-sm font-semibold">{field.label}</label>
          <input id={field.name} name={field.name} type={field.type} required={field.required} maxLength={'maxLength' in field ? field.maxLength : undefined} pattern={field.type === 'url' ? '[hH][tT][tT][pP][sS]?://.+' : undefined} className="w-full rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 text-gray-100" aria-describedby={field.name === 'companyType' ? 'type-hint' : undefined} />
          {field.name === 'companyType' && <p id="type-hint" className="mt-2 text-xs text-gray-300">Enter the company’s industry, such as Financial services, Telecommunications, or Healthcare.</p>}
        </div>)}
        <button type="submit" className="w-full rounded-md bg-gray-100 px-4 py-3 font-semibold text-gray-900 hover:bg-gray-300">{pending ? 'Adding company…' : 'Add company'}</button>
      </fieldset>
      {!pending && <Link to="/companies" className="mt-4 inline-block text-sm underline">Cancel</Link>}
    </form>
  </main>
}

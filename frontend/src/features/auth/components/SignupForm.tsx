import { useEffect, useRef, useState } from 'react'
import type { SubmitEvent } from 'react'
import { signup, SignupError } from '../api/signup'

const fields = [
  { name: 'email', label: 'Email address', type: 'email', autoComplete: 'email', maxLength: 254 },
  { name: 'username', label: 'Username', type: 'text', autoComplete: 'username', maxLength: 50, pattern: '[a-z0-9_\\-]+', hint: 'Use lowercase letters, numbers, underscores, or hyphens.' },
  { name: 'password', label: 'Password', type: 'password', autoComplete: 'new-password', minLength: 8, maxLength: 72, hint: 'Use 8–72 characters.' },
  { name: 'confirmPassword', label: 'Confirm password', type: 'password', autoComplete: 'new-password', maxLength: 72 },
] as const

export default function SignupForm() {
  const [pending, setPending] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState<SignupError | null>(null)
  const request = useRef<AbortController | null>(null)
  const feedback = useRef<HTMLDivElement>(null)

  useEffect(() => () => request.current?.abort(), [])
  useEffect(() => { if (error || success) feedback.current?.focus() }, [error, success])

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (request.current) return
    const form = event.currentTarget
    const data = new FormData(form)
    const details = {
      email: String(data.get('email')).trim(),
      username: String(data.get('username')),
      password: String(data.get('password')),
      confirmPassword: String(data.get('confirmPassword')),
    }
    if (details.password !== details.confirmPassword) {
      setError(new SignupError('Please check your password confirmation.', { confirmPassword: ['Passwords must match.'] }))
      return
    }
    const controller = new AbortController()
    request.current = controller
    setPending(true)
    setError(null)
    try {
      await signup(details, controller.signal)
      form.reset()
      setSuccess(true)
    } catch (failure) {
      if (controller.signal.aborted) return
      setError(failure instanceof SignupError ? failure : new SignupError('Unable to reach the server. Check your connection and try again.'))
    } finally {
      request.current = null
      if (!controller.signal.aborted) setPending(false)
    }
  }

  if (success) return (
    <div ref={feedback} tabIndex={-1} role="status" className="rounded-lg border border-green-700 bg-green-950 p-5 text-green-100">
      <h2 className="font-bold">Account created</h2>
      <p className="mt-2 text-sm">Your account is ready. You are not signed in yet.</p>
    </div>
  )

  return (
    <form onSubmit={handleSubmit} aria-busy={pending}>
      {error && <div ref={feedback} tabIndex={-1} role="alert" className="mb-5 rounded-md border border-red-400 bg-red-950 p-4 text-sm text-red-100">
        <p>{error.message}</p>
        {Object.entries(error.violations).filter(([name]) => !fields.some(field => field.name === name)).map(([name, messages]) => <p key={name}>{messages.join(' ')}</p>)}
      </div>}
      <fieldset disabled={pending} className="space-y-5 disabled:opacity-70">
        {fields.map(field => {
          const messages = error?.violations[field.name]
          const hint = 'hint' in field ? field.hint : undefined
          return <div key={field.name}>
            <label htmlFor={field.name} className="mb-2 block text-sm font-semibold">{field.label}</label>
            <input
              id={field.name} name={field.name} type={field.type} autoComplete={field.autoComplete}
              maxLength={field.maxLength} minLength={'minLength' in field ? field.minLength : undefined}
              pattern={'pattern' in field ? field.pattern : undefined} required
              aria-invalid={!!messages}
              aria-describedby={[hint && `${field.name}-hint`, messages && `${field.name}-error`].filter(Boolean).join(' ') || undefined}
              className="w-full rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 text-gray-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-100"
            />
            {hint && <p id={`${field.name}-hint`} className="mt-2 text-xs text-gray-300">{hint}</p>}
            {messages && <p id={`${field.name}-error`} className="mt-2 text-sm text-red-300">{messages.join(' ')}</p>}
          </div>
        })}
        <button type="submit" className="w-full cursor-pointer rounded-md bg-gray-100 px-4 py-3 font-semibold text-gray-900 hover:bg-gray-300 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-100 disabled:cursor-wait">
          {pending ? 'Creating account…' : 'Create account'}
        </button>
      </fieldset>
    </form>
  )
}

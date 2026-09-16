import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { login } from '../api/session'

export default function LoginForm({ onLogin }: { onLogin: () => void }) {
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (pending) return
    const form = event.currentTarget
    const data = new FormData(form)
    setPending(true)
    setError('')
    try {
      await login({ email: String(data.get('email')).trim(), password: String(data.get('password')) })
      form.reset()
      onLogin()
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Unable to log in. Please try again.')
    } finally {
      setPending(false)
    }
  }

  return (
  <form onSubmit={handleSubmit} aria-busy={pending}>
    {error && <p role="alert" className="mb-5 rounded-md border border-red-400 bg-red-950 p-4 text-sm text-red-100">{error}</p>}
    <fieldset disabled={pending} className="space-y-5 disabled:opacity-70">
      <div>
        <label htmlFor="login-email" className="mb-2 block text-sm font-semibold">Email address</label>
        <input id="login-email" name="email" type="email" autoComplete="username" required maxLength={254} className="w-full rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 text-gray-100 focus-visible:outline-2 focus-visible:outline-offset-2" />
      </div>
      <div>
        <label htmlFor="login-password" className="mb-2 block text-sm font-semibold">Password</label>
        <input id="login-password" name="password" type="password" autoComplete="current-password" required className="w-full rounded-md border border-gray-500 bg-gray-700 px-3 py-2.5 text-gray-100 focus-visible:outline-2 focus-visible:outline-offset-2" />
      </div>
      <button type="submit" className="w-full cursor-pointer rounded-md bg-gray-100 px-4 py-3 font-semibold text-gray-900 hover:bg-gray-300 focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-wait">{pending ? 'Logging in…' : 'Log in'}</button>
    </fieldset>
  </form>
  )
}

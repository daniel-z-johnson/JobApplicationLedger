import { Link } from 'react-router'
import LoginForm from './components/LoginForm'

export default function LoginPage({ onLogin }: { onLogin: () => void }) {
  return (
    <main className="mx-auto max-w-lg px-4 py-10 sm:py-16">
      <section className="rounded-xl border border-gray-600 bg-gray-800 p-6 shadow-lg sm:p-8" aria-labelledby="login-heading">
        <h1 id="login-heading" className="mb-8 text-2xl font-bold">Log in</h1>
        <LoginForm onLogin={onLogin} />
        <p className="mt-6 text-sm text-gray-300">Need an account? <Link to="/signup" className="underline">Sign up</Link></p>
      </section>
    </main>
  )
}

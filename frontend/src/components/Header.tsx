import { Link } from 'react-router'

export default function Header() {
  return (
    <header className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-700 bg-gray-800 px-4 py-4 sm:px-8">
      <Link to="/" className="text-lg font-bold text-gray-100 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-gray-300">
        Job Application Ledger
      </Link>

      <nav aria-label="Account" className="ml-auto flex items-center gap-3">
        <button
          type="button"
          className="cursor-pointer rounded-md px-4 py-2 text-sm font-semibold text-gray-100 transition-colors hover:bg-gray-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-300"
        >
          Login
        </button>
        <Link
          to="/signup"
          className="cursor-pointer rounded-md bg-gray-100 px-4 py-2 text-sm font-semibold text-gray-900 transition-colors hover:bg-gray-300 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-300"
        >
          Sign up
        </Link>
      </nav>
    </header>
  )
}

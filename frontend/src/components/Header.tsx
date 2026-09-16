import { Link } from 'react-router'

export default function Header({ signedIn, loading, loggingOut, onLogout }: {
  signedIn: boolean
  loading: boolean
  loggingOut: boolean
  onLogout: () => void
}) {
  return (
    <header className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-700 bg-gray-800 px-4 py-4 sm:px-8">
      <Link to="/" className="text-lg font-bold text-gray-100 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-gray-300">
        Job Application Ledger
      </Link>

      <nav aria-label="Account" className="ml-auto flex items-center gap-3">
        {signedIn ? <>
          <Link to="/profile" className="rounded-md px-4 py-2 text-sm font-semibold text-gray-100 hover:bg-gray-700">Profile</Link>
          <button type="button" onClick={onLogout} disabled={loggingOut} className="flex cursor-pointer items-center gap-2 rounded-md bg-gray-100 px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-300 focus-visible:outline-2 focus-visible:outline-offset-2 disabled:opacity-70">
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5"><path strokeLinecap="round" strokeLinejoin="round" d="M9 5H5v14h4M14 8l4 4-4 4M9 12h9" /></svg>
            {loggingOut ? 'Logging out…' : 'Logout'}
          </button>
        </> : !loading && <>
        <Link
          to="/login"
          className="cursor-pointer rounded-md px-4 py-2 text-sm font-semibold text-gray-100 transition-colors hover:bg-gray-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-300"
        >
          Login
        </Link>
        <Link
          to="/signup"
          className="cursor-pointer rounded-md bg-gray-100 px-4 py-2 text-sm font-semibold text-gray-900 transition-colors hover:bg-gray-300 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-300"
        >
          Sign up
        </Link>
        </>}
      </nav>
    </header>
  )
}

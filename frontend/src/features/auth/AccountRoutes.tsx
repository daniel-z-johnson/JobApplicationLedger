import { Navigate, Outlet } from 'react-router'

type SessionGateProps = {
  signedIn: boolean
  loading: boolean
  error: string
  retryDisabled: boolean
  onRetry: () => void
}

// Wait for the session check before rendering pages or making redirects.
export function SessionGate({ signedIn, loading, error, retryDisabled, onRetry }: SessionGateProps) {
  if (loading) return <main className="p-8" role="status">Loading account…</main>

  return <>
    {error && <div role="alert" className="mx-auto mt-6 max-w-lg rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{error} <button type="button" onClick={onRetry} disabled={retryDisabled} className="underline disabled:opacity-70">Retry session check</button></div>}
    {(!error || signedIn) && <Outlet />}
  </>
}

export function SignedInRoute({ signedIn }: { signedIn: boolean }) {
  return signedIn ? <Outlet /> : <Navigate to="/login" replace />
}

export function SignedOutRoute({ signedIn }: { signedIn: boolean }) {
  return signedIn ? <Navigate to="/profile" replace /> : <Outlet />
}

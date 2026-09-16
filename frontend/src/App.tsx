import { useEffect, useState } from 'react'
import ProfilePage from './features/profile/ProfilePage'
import Header from './components/Header'
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router'
import SignupPage from './features/auth/SignupPage'
import LoginForm from './features/auth/LoginForm'
import { getProfile, logout } from './features/auth/api/session'
import type { Profile } from './features/auth/api/session'
import './App.css'

function App() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [loggingOut, setLoggingOut] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const controller = new AbortController()
    getProfile(controller.signal).then(setProfile).catch(() => {
      if (!controller.signal.aborted) setError('Unable to load your profile. Please try again.')
    }).finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [])

  async function refreshProfile() {
    setLoading(true)
    setError('')
    try {
      const current = await getProfile()
      setProfile(current)
      navigate(current ? '/profile' : '/login', { replace: true })
    } catch {
      setError('Unable to load your profile. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  async function handleLogout() {
    if (loggingOut) return
    setLoggingOut(true)
    setError('')
    try {
      await logout()
      setProfile(null)
      navigate('/login', { replace: true })
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Unable to log out. Please try again.')
    } finally {
      setLoggingOut(false)
    }
  }

  return <>
    <Header signedIn={!!profile} loading={loading || (!!error && !profile)} loggingOut={loggingOut} onLogout={handleLogout} />
    {error && <div role="alert" className="mx-auto mt-6 max-w-lg rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{error} <button type="button" onClick={refreshProfile} className="underline">Retry</button></div>}
    {loading ? <main className="p-8" role="status">Loading account…</main> : error && !profile ? null : <Routes>
      <Route path="/" element={profile ? <ProfilePage profile={profile} /> : null} />
      <Route path="/login" element={profile ? <Navigate to="/profile" replace /> : <LoginForm onLogin={refreshProfile} />} />
      <Route path="/profile" element={profile ? <ProfilePage profile={profile} /> : <Navigate to="/login" replace />} />
      <Route path="/signup" element={profile ? <Navigate to="/profile" replace /> : <SignupPage />} />
      <Route path="*" element={<main className="p-8"><h1 className="text-2xl font-bold">Page not found</h1><Link to="/" className="mt-4 inline-block underline">Return home</Link></main>} />
    </Routes>}
  </>
}

export default App

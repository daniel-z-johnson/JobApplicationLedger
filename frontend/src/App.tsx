import ProfilePage from './features/profile/ProfilePage'
import Header from './components/Header'
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router'
import SignupPage from './features/auth/SignupPage'
import LoginForm from './features/auth/LoginForm'
import { useAuth } from './features/auth/useAuth'
import './App.css'

function App() {
  const { signedIn, loading, sessionError, logoutError, loggingOut, retrySession, completeLogin, sessionExpired, handleLogout } = useAuth()
  const navigate = useNavigate()

  function handleLogin() {
    completeLogin()
    navigate('/profile', { replace: true })
  }

  async function onLogout() {
    if (await handleLogout()) navigate('/login', { replace: true })
  }

  return <>
    <Header signedIn={signedIn} loading={loading || (!!sessionError && !signedIn)} loggingOut={loggingOut} onLogout={onLogout} />
    {sessionError && <div role="alert" className="mx-auto mt-6 max-w-lg rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{sessionError} <button type="button" onClick={retrySession} disabled={loggingOut} className="underline disabled:opacity-70">Retry session check</button></div>}
    {logoutError && <div role="alert" className="mx-auto mt-6 max-w-lg rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{logoutError} <button type="button" onClick={onLogout} disabled={loggingOut} className="underline disabled:opacity-70">Retry logout</button></div>}
    {loading ? <main className="p-8" role="status">Loading account…</main> : sessionError && !signedIn ? null : <Routes>
      <Route path="/" element={signedIn ? <Navigate to="/profile" replace /> : null} />
      <Route path="/login" element={signedIn ? <Navigate to="/profile" replace /> : <LoginForm onLogin={handleLogin} />} />
      <Route path="/profile" element={signedIn ? <ProfilePage onSessionExpired={sessionExpired} /> : <Navigate to="/login" replace />} />
      <Route path="/signup" element={signedIn ? <Navigate to="/profile" replace /> : <SignupPage />} />
      <Route path="*" element={<main className="p-8"><h1 className="text-2xl font-bold">Page not found</h1><Link to="/" className="mt-4 inline-block underline">Return home</Link></main>} />
    </Routes>}
  </>
}

export default App

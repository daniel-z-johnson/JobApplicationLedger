import ProfilePage from './features/profile/ProfilePage'
import CompaniesPage from './features/companies/CompaniesPage'
import AddCompanyPage from './features/companies/AddCompanyPage'
import CompanyDetailPage from './features/companies/CompanyDetailPage'
import Header from './components/Header'
import Sidebar from './components/Sidebar'
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router'
import SignupPage from './features/auth/SignupPage'
import LoginPage from './features/auth/LoginPage'
import { useAuth } from './features/auth/useAuth'
import { SessionGate, SignedInRoute, SignedOutRoute } from './features/auth/AccountRoutes'
import './App.css'

function App() {
  const { signedIn, loading, sessionError, logoutError, loggingOut, retrySession, completeLogin, sessionExpired, handleLogout } = useAuth()
  const navigate = useNavigate()

  function handleLogin() {
    completeLogin()
    navigate('/companies', { replace: true })
  }

  async function onLogout() {
    if (await handleLogout()) navigate('/login', { replace: true })
  }

  return <div className="flex min-h-dvh flex-col">
    <Header signedIn={signedIn} loading={loading || (!!sessionError && !signedIn)} loggingOut={loggingOut} onLogout={onLogout} />
    <div className="flex flex-1">
    {signedIn && <Sidebar />}
    <div className="min-w-0 flex-1">
    {logoutError && <div role="alert" className="mx-auto mt-6 max-w-lg rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{logoutError} <button type="button" onClick={onLogout} disabled={loggingOut} className="underline disabled:opacity-70">Retry logout</button></div>}
    <Routes>
      <Route element={<SessionGate signedIn={signedIn} loading={loading} error={sessionError} retryDisabled={loggingOut} onRetry={retrySession} />}>
        <Route path="/" element={signedIn ? <Navigate to="/companies" replace /> : null} />
        <Route element={<SignedOutRoute signedIn={signedIn} />}>
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="/signup" element={<SignupPage />} />
        </Route>
        <Route element={<SignedInRoute signedIn={signedIn} />}>
          <Route path="/companies" element={<CompaniesPage onSessionExpired={sessionExpired} />} />
          <Route path="/companies/new" element={<AddCompanyPage onSessionExpired={sessionExpired} />} />
          <Route path="/companies/:companyId" element={<CompanyDetailPage onSessionExpired={sessionExpired} />} />
          <Route path="/profile" element={<ProfilePage onSessionExpired={sessionExpired} />} />
        </Route>
        <Route path="*" element={<main className="p-8"><h1 className="text-2xl font-bold">Page not found</h1><Link to="/" className="mt-4 inline-block underline">Return home</Link></main>} />
      </Route>
    </Routes>
    </div>
    </div>
  </div>
}

export default App

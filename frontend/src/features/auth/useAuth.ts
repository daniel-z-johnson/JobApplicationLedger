import { useCallback, useEffect, useRef, useState } from 'react'
import { checkSession, logout } from './api/session'

export function useAuth() {
  const [signedIn, setSignedIn] = useState(false)
  const [loading, setLoading] = useState(true)
  const [sessionError, setSessionError] = useState('')
  const [logoutError, setLogoutError] = useState('')
  const [loggingOut, setLoggingOut] = useState(false)
  const [sessionAttempt, setSessionAttempt] = useState(0)
  const sessionRequest = useRef<AbortController | null>(null)
  const logoutPending = useRef(false)

  useEffect(() => {
    const controller = new AbortController()
    sessionRequest.current = controller
    checkSession(controller.signal).then(current => {
      if (!controller.signal.aborted) setSignedIn(current)
    }).catch(() => {
      if (!controller.signal.aborted) setSessionError('Unable to check your session. Please try again.')
    }).finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [sessionAttempt])

  function retrySession() {
    setLoading(true)
    setSessionError('')
    setSessionAttempt(attempt => attempt + 1)
  }

  function completeLogin() {
    sessionRequest.current?.abort()
    setSignedIn(true)
    setLoading(false)
    setSessionError('')
    setLogoutError('')
  }

  const sessionExpired = useCallback(() => {
    setSignedIn(false)
    setLogoutError('')
  }, [])

  async function handleLogout(): Promise<boolean> {
    if (logoutPending.current) return false
    logoutPending.current = true
    sessionRequest.current?.abort()
    setLoggingOut(true)
    setLogoutError('')
    try {
      await logout()
      setSignedIn(false)
      return true
    } catch (failure) {
      setLogoutError(failure instanceof Error ? failure.message : 'Unable to log out. Please try again.')
      return false
    } finally {
      logoutPending.current = false
      setLoggingOut(false)
    }
  }

  return { signedIn, loading, sessionError, logoutError, loggingOut, retrySession, completeLogin, sessionExpired, handleLogout }
}

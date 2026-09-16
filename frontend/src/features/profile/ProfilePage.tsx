import { useEffect, useState } from 'react'
import { getProfile } from './api/profile'
import type { Profile } from './api/profile'

export default function ProfilePage({ onSessionExpired }: { onSessionExpired: () => void }) {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    getProfile(controller.signal).then(current => {
      if (controller.signal.aborted) return
      if (!current) onSessionExpired()
      else setProfile(current)
    }).catch(() => {
      if (!controller.signal.aborted) setError('Unable to load your profile. Please try again.')
    }).finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [attempt, onSessionExpired])

  function retry() {
    setLoading(true)
    setError('')
    setAttempt(value => value + 1)
  }

  return (
    <main className="mx-auto max-w-lg px-4 py-10 sm:py-16">
      <section className="rounded-xl border border-gray-600 bg-gray-800 p-6 shadow-lg sm:p-8" aria-labelledby="profile-heading">
        <h1 id="profile-heading" className="text-2xl font-bold">Profile</h1>
        {loading ? <p role="status" className="mt-6">Loading profile…</p> : error ?
          <div role="alert" className="mt-6 rounded-md border border-red-400 bg-red-950 p-4 text-red-100">{error} <button type="button" onClick={retry} className="underline">Retry loading profile</button></div> :
          profile && <dl className="mt-6"><dt className="text-sm text-gray-300">Username</dt><dd className="mt-2 break-words text-lg font-semibold">{profile.username}</dd></dl>}
      </section>
    </main>
  )
}

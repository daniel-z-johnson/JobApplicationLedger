import { apiRequest } from '../../../api/request'

export type Profile = { username: string }

export async function getProfile(signal?: AbortSignal): Promise<Profile | null> {
  const response = await apiRequest('/u/me', { signal })
  if (response.status === 401) return null
  if (!response.ok) throw new Error('Unable to load your profile. Please try again.')
  const body = await response.json()
  if (typeof body.username !== 'string') throw new Error('Unable to load your profile. Please try again.')
  return { username: body.username }
}

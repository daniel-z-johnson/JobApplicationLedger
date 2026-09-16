import { CsrfError } from '../../../api/csrf'
import { apiRequest } from '../../../api/request'

export type SignupRequest = {
  email: string
  username: string
  password: string
  confirmPassword: string
}

export class SignupError extends Error {
  violations: Record<string, string[]>

  constructor(message: string, violations: Record<string, string[]> = {}) {
    super(message)
    this.violations = violations
  }
}

async function registrationError(response: Response): Promise<SignupError> {
  if (response.status === 403) return new SignupError('Your security token could not be verified. Please submit again to get a fresh token.')
  if (response.status >= 500) return new SignupError('The server could not complete signup. Please try again later.')
  const body = await response.json().catch(() => null)
  const violations: Record<string, string[]> = {}
  if (body?.violations && typeof body.violations === 'object') {
    for (const [field, messages] of Object.entries(body.violations)) {
      if (Array.isArray(messages) && messages.every(message => typeof message === 'string')) {
        violations[field] = messages
      }
    }
  }
  return new SignupError(
    typeof body?.message === 'string' ? body.message : 'Unable to create your account. Please try again.',
    violations,
  )
}

export async function signup(details: SignupRequest, signal?: AbortSignal): Promise<void> {
  const response = await apiRequest('/u/register', {
    method: 'POST',
    json: details,
    signal,
  }).catch((error: unknown) => {
    if (error instanceof CsrfError) throw new SignupError(error.message)
    throw error
  })
  if (!response.ok) throw await registrationError(response)
}

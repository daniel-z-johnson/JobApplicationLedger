import SignupForm from './components/SignupForm'

export default function SignupPage() {
  return (
    <main className="mx-auto max-w-lg px-4 py-10 sm:py-16">
      <section className="rounded-xl border border-gray-600 bg-gray-800 p-6 shadow-lg sm:p-8" aria-labelledby="signup-heading">
        <h1 id="signup-heading" className="text-2xl font-bold">Create your account</h1>
        <p className="mb-8 mt-3 text-sm text-gray-300">Keep your job search organized in one place.</p>
        <SignupForm />
      </section>
    </main>
  )
}

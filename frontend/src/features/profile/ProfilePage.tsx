export default function ProfilePage({ profile }: { profile: { username: string } | null }) {
  return (
    <main className="mx-auto max-w-lg px-4 py-10 sm:py-16">
      <section className="rounded-xl border border-gray-600 bg-gray-800 p-6 shadow-lg sm:p-8" aria-labelledby="profile-heading">
        <h1 id="profile-heading" className="text-2xl font-bold">Profile</h1>
        <dl className="mt-6"><dt className="text-sm text-gray-300">Username</dt><dd className="mt-2 break-words text-lg font-semibold">{profile?.username}</dd></dl>
      </section>
    </main>
  )
}
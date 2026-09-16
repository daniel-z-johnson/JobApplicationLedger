import Header from './components/Header'
import { Link, Route, Routes } from 'react-router'
import SignupPage from './features/auth/SignupPage'
import './App.css'

function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={null} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="*" element={<main className="p-8"><h1 className="text-2xl font-bold">Page not found</h1><Link to="/" className="mt-4 inline-block underline">Return home</Link></main>} />
      </Routes>
    </>
  )
}

export default App

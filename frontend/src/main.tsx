import { StrictMode } from 'react'
import { BrowserRouter } from 'react-router'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <div className='min-h-dvh w-full bg-[#606060] text-gray-100'>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </div>
  </StrictMode>,
)

import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, import.meta.dirname, 'DEV_API_')

  return {
    plugins: [react(), tailwindcss()],
    server: {
      proxy: {
        '^/api(?:/|$)': {
          target: env.DEV_API_TARGET || 'http://localhost:8080',
          // Spring Boot owns the /api context path; preserve it.
        },
      },
    },
  }
})

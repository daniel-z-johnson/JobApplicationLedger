# Job Application Ledger frontend

## Environment configuration

- `.env` provides the public API base path (`/api`) for all modes.
- `.env.development` points the Vite development proxy at `http://localhost:8080`.
- Optionally copy `.env.example` to `.env.local` for personal overrides. Git ignores `.env.local`.
- Restart the development server after changing environment files.
- `VITE_` values are public and baked into the build. Never put secrets in them.

Run `npm run dev` with Spring Boot running on port 8080. Requests to
`/api/...` are forwarded to `http://localhost:8080/api/...` without removing
the prefix. `DEV_API_TARGET` is the backend origin, without `/api`.

API code imports `env` from `src/config/env.ts` and appends an endpoint path
to `env.apiBaseUrl`.

## Signup

The header links to `/signup`. Submission first fetches `/api/u/csrf`, then
sends JSON to `/api/u/register` with the raw `XSRF-TOKEN` cookie value in
the `X-XSRF-TOKEN` header. Spring's SPA handler expects the raw cookie token,
not the masked token returned in the CSRF response body. The CSRF cookie has
path `/` so the frontend can read it outside `/api`.

Registration does not sign the user in. Login is not connected yet. Form values
are not logged or saved in browser storage; a successful submission clears the form.
Run `npm test` for signup API tests, `npm run lint`, and `npm run build`.

In production, configure the reverse proxy to send `/api` and `/api/...` to
Spring Boot, preserving the path, and serve React for other routes. Vite's
development proxy is not part of the production build.

## Vite template reference

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

You can also install [eslint-plugin-react-x](https://npmx.dev/package/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://npmx.dev/package/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

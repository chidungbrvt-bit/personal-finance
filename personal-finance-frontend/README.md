# Personal Finance Frontend

Responsive React dashboard for the Personal Finance Spring Boot backend. It uses the real REST API and JWT authentication; there is no mock data.

## Stack

- React + TypeScript + Vite
- React Router and Axios
- Tailwind CSS
- Recharts and Lucide React

## Run locally

Requirements: Node.js 20.19+ (or 22.12+) and npm or pnpm.

1. Start the Spring Boot backend on `http://localhost:8080`.
2. Run `npm install` (or `pnpm install`).
3. Copy `.env.example` to `.env` if you want to change the defaults.
4. Run `npm run dev` (or `pnpm dev`).
5. Open the frontend and create a user on `/register`. Save the one-time recovery code before continuing.

With `VITE_API_BASE_URL` empty, Vite proxies `/api` requests to `http://localhost:8080`, avoiding a CORS problem locally. For deployment, set it to the public backend URL and configure Spring CORS for the frontend origin.

## Environment variables

```env
VITE_API_BASE_URL=
```

When `VITE_API_BASE_URL` is empty in development, requests use Vite's backend proxy. For a deployed frontend, set it to the backend's public origin.

## Pages

- Register and login: create a user, receive a JWT and display that user's recovery code once
- Reset password: enter the account email, its private recovery code and a new password
- Dashboard: monthly summary, balances, budget progress and recent transactions
- Accounts: create, edit, remove/archive, restore and view computed balances
- Categories: create, edit, delete and group by income/expense
- Transactions: CRUD plus account/category/type and custom-date filtering
- Budgets: CRUD and monthly progress

All monetary input and display is fixed to VND to match the current backend rule.

Protected API requests automatically send the JWT as an `Authorization: Bearer ...` header and use the authenticated user's ID. A `401 Unauthorized` response clears the local session and returns the user to the login page.

## Checks

```bash
npm run lint
npm run build
```

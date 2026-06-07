# EventFlow Frontend

This is a minimal React + Vite + TypeScript frontend scaffold for the EventFlow API.

Quick start

1. Install dependencies

```bash
cd frontend
npm install
```

2. Run dev server

```bash
npm run dev
```

By default Vite runs on port 3000. The frontend expects the API to be available at `/api/v1` (same host) — use a proxy or configure CORS on the backend if running the frontend and backend on different ports.

Notes

- This scaffold provides placeholder pages and a small `auth` service that stores the JWT in `localStorage`.
- Implement full forms, validation, and API integration per page requirements.
- For a production-ready app, add styling (Tailwind or component library), tests, and build/deployment scripts.

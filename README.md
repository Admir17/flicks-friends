# Flicks & Friends

Full-stack social movie platform with watch parties, chat, ratings, and personal watchlists.

This repository is a **monorepo** containing both frontend and backend applications.

---

## Repository Structure

```text
flicks-friends/
├── frontend/   # Next.js client
├── backend/    # Spring Boot API
└── docs/       # Optional: architecture docs, ADRs, diagrams
```

## Run Locally

### 1) Backend

```bash
cd backend
cp .env.example .env
# fill in .env values
bash run-local.sh
```

Notes:
- Backend runs on `http://localhost:8080` by default.
- `TMDB_API_KEY` and `TMDB_API_TOKEN` are required for movie endpoints.
- `SMTP_USERNAME` and `SMTP_PASSWORD` are optional (invite emails are skipped if missing).

### 2) Frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend will run on `http://localhost:3000` and talk to backend `http://localhost:8080` in development mode.

## Quick Test Commands

Backend tests:

```bash
cd backend
bash gradlew test
```

Frontend build:

```bash
cd frontend
npm run build
```

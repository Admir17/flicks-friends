# Flicks & Friends – Frontend + Backend Review and Improvement Plan

## Executive Summary
The project has a strong product base (feature scope, UI idea, and domain fit), but backend stability is currently limited by operational hardening gaps (configuration, error handling, observability, and defensive validation). For job applications, a focused stability pass is higher value than adding new features.

## Current Critique

### Backend
1. **Secrets in repository history**
   - API keys and SMTP credentials were committed in `application.properties`.
   - Risks: security exposure, poor environment parity, and difficult reproducibility.

2. **Fragile email flow**
   - Email sending is tightly coupled to invite logic.
   - Failures were previously surfaced only by stack traces, which makes operations and debugging harder.

3. **Inconsistent exception handling strategy**
   - Two global exception handlers make error responses less predictable.
   - Not always a direct crash root-cause, but a real maintenance and debugging cost.

4. **Insufficient defensive input validation**
   - Some service methods assume valid input.
   - Edge cases can still produce runtime failures.

5. **No explicit production-readiness baseline**
   - Missing clear Definition of Done for stability (health checks, timeouts, retries, structured logs, etc.).

### Frontend
1. **Inconsistent token storage handling**
   - Auth hook and API layer used different storage sources.
   - Symptom: intermittent “logged in but still 401” behavior.

2. **Code hygiene / naming consistency**
   - Small naming inconsistencies reduce professionalism for portfolio presentation.

3. **No centralized error UX pattern**
   - Error states and recovery patterns (retry/guidance/telemetry) are not consistently applied.

## Improvements Already Applied in This Repository
1. **Externalized secrets**
   - SMTP and TMDB credentials are now loaded from environment variables.
2. **Hardened watch party service**
   - Added defensive validation when creating watch parties.
   - Replaced `System.out` with SLF4J logging.
   - URL-encoded invite usernames.
   - Gracefully skips email sending if SMTP credentials are missing.
3. **Stabilized frontend auth behavior**
   - API service accepts token from `sessionStorage` **or** `localStorage`.

## Recommended “Clean & Stable” Roadmap (2–3 Weeks)

### Phase 1 – Crash and Stability Focus (Week 1)
- Unify global exception handling into one predictable strategy.
- Add controller-boundary validation (`@Valid`, DTO constraints).
- Standardize HTTP error payload shape for frontend consumption.
- Add request correlation IDs and structured logging.
- Add smoke tests for critical flows (login, create watch party, invite, watchlist).

### Phase 2 – Quality and Clean Code (Week 2)
- Decouple service boundaries (email via queue/async boundary or dedicated adapter).
- Expand repository/service tests for edge cases and race conditions.
- Frontend: central API error mapping + reusable toast/retry components.
- Enforce naming consistency and remove dead code.

### Phase 3 – Monorepo Polish + Portfolio Narrative (Week 3)
- Improve root-level developer experience:
  - `Makefile`/`justfile` targets for `dev`, `test`, `lint`, and `ci`.
  - One central `README` with architecture and runbook.
- Add CI workflow (backend tests + frontend lint/build + optional e2e smoke).
- Build a concise portfolio README:
  - Problem → solution → architecture → quality practices → lessons learned.

## Monorepo Consolidation Guidelines
- Keep one clear source of truth per layer:
  - `/backend` for Spring Boot
  - `/frontend` for Next.js
  - `/docs` for architecture, ADRs, and incident postmortems
- Keep `main` stable and use small feature branches.
- Add a PR template with mandatory fields:
  - risk assessment
  - test evidence
  - rollback plan

## Measurable Quality Targets
- 0 hardcoded secrets in tracked files.
- No uncaught exceptions on critical API endpoints in normal operation.
- Documented p95 latency and error rates for core endpoints.
- CI stays green for backend tests and frontend lint/build.
- One concise architecture/operations document available.

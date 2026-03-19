# Contributing

Thank you for contributing to this project.

## Language Policy (Mandatory)

All repository artifacts must be in **English**, including:
- documentation
- code comments
- commit messages
- pull request titles and descriptions
- issue titles and descriptions

---

## Branching Strategy

- `main`: stable branch
- Feature branches:
  - `feat/<short-description>`
  - `fix/<short-description>`
  - `refactor/<short-description>`
  - `docs/<short-description>`
  - `chore/<short-description>`

Examples:
- `feat/add-watchparty-validation`
- `fix/login-error-handling`
- `docs/update-setup-guide`

---

## Commit Message Convention

Use **Conventional Commits**:

- `feat:`
- `fix:`
- `refactor:`
- `docs:`
- `test:`
- `chore:`

Examples:
- `feat(auth): add OTP verification endpoint`
- `fix(api): return 400 for invalid watchparty payload`
- `docs(readme): improve local setup instructions`

---

## Pull Request Requirements

Before opening a pull request, ensure:

- [ ] Title and description are in English
- [ ] Scope is focused and reasonably small
- [ ] No secrets (API keys, passwords, tokens) are committed
- [ ] Relevant tests were run locally
- [ ] Documentation is updated if behavior changed
- [ ] Breaking changes are explicitly stated

---

## Code Quality Guidelines

- Keep controllers/routes thin and move business logic into services
- Validate incoming payloads at API boundaries
- Avoid broad catch-all exception handling where possible
- Prefer structured logging over `printStackTrace`
- Do not construct JSON manually with string concatenation
- Add or update tests when changing behavior

---

## Security Guidelines

- Never commit real credentials or secrets
- Use environment variables / secret management
- Rotate any compromised secret immediately
- Keep dependencies up to date

---

## How to Report Bugs

Include:
1. expected behavior
2. actual behavior
3. steps to reproduce
4. relevant logs/screenshots
5. environment info (OS, runtime, versions) if relevant
# RBAC + Service-to-Service Integration — Teaching Notes

This document explains the changes made to add role-based access control (ADMIN vs
EMPLOYEE) across the microservices, and to wire `employee-service` to
`department-service`/`organization`, and `department-service` to `organization`,
with proper permission checks. Use this as a walkthrough with students; it covers
**what changed, why, and how to test it end-to-end**.

## 1. The role model

Two roles exist, both plain rows in `user-service`'s `roles` table:

- **`ROLE_ADMIN`** — full read/write on organizations, departments, employees; can
  approve/reject leave requests.
- **`ROLE_EMPLOYEE`** — the default role every self-registered account gets. Can
  read directory data (organizations/departments/employees) and apply for /
  view leave requests. Cannot create/update/delete organizations, departments,
  employees, or approve leave.

A seeded admin account exists out of the box so there is a way in before any
admin is created by hand:

```
username: admin
email:    admin@example.com
password: Admin@123
```

This is created by `user-service`'s `AdminBootstrapConfig` on first startup —
**change or remove this before any real deployment.**

## 2. How authorization flows without a gateway

There is no API gateway or service registry in this system (no Eureka, no
Spring Cloud Gateway) — every service runs standalone on a fixed port and
calls the others by hardcoded URL. That has one important consequence for
security: **every service must independently verify the JWT**, because there
is no single choke point to do it once.

The mechanism:

1. `user-service` is the only service that *issues* tokens (`/api/auth/login`,
   `/register`, `/refresh`). It signs a JWT with an HMAC secret
   (`app.jwt.secret`) and embeds the user's roles as a `roles` claim.
2. Every other service (`organization`, `department-service`, `employee-service`,
   `leave-servcie`) has its own copy of a small `security` package
   (`JwtUtil` + `JwtAuthenticationFilter`) that **verifies** the token using the
   *same* secret and reads the `roles` claim directly — no database lookup, no
   callback to `user-service`. This only works because `app.jwt.secret` is
   byte-identical across all six services' config files.
3. Controllers use `@PreAuthorize("hasRole('ADMIN')")` on write endpoints;
   reads just require `.anyRequest().authenticated()`.
4. When one service calls another on the caller's behalf (e.g. `employee-service`
   asking `department-service` "does department 3 exist?"), the callee also
   requires a valid JWT. So the caller **forwards the original `Authorization`
   header** on its outbound `WebClient` call (see `DirectoryClient` in
   `department-service` and `employee-service`, and `LeaveServiceImpl` in
   `leave-servcie`). Without a gateway, this forwarding has to happen by hand
   at every call site — that repetition is exactly the pain a gateway removes
   (see §6, exercise 1).

## 3. Per-service changes

### `user-service` (port 9001, Postgres `userservice`)

- `JwtUtil.generateAccessToken(username, roles)` now embeds a `roles` claim in
  every issued token (was: subject/username only).
- Self-registration (`POST /api/auth/register`) now assigns `ROLE_EMPLOYEE`
  instead of the old `ROLE_USER` (rename for domain clarity — this is an HR
  system, "employee" is the right default noun).
- New admin-only endpoints on `AuthController`:
  - `GET /api/auth/users` — list every account.
  - `PUT /api/auth/users/{id}/role` — change a user's role, body
    `{"role": "ROLE_ADMIN"}`. This is how you promote someone to admin; there
    is intentionally no public "register as admin" endpoint.
- `AdminBootstrapConfig` (new): seeds `ROLE_ADMIN`/`ROLE_EMPLOYEE` and the
  default admin account described above.
- `SecurityConfig` now has `@EnableMethodSecurity` so `@PreAuthorize` is honored.

### `organization` (port 9002, MySQL `organization_db`)

- **Fixed a broken build**: `pom.xml` referenced a Spring Boot parent version
  that doesn't exist (`4.1.0`) and artifacts that don't exist
  (`spring-boot-starter-webmvc`, `-data-jpa-test`, `-validation-test`,
  `-webmvc-test`). This service could not have compiled before this change.
  Fixed to parent `3.5.16` and real artifacts (`spring-boot-starter-web`,
  `spring-boot-starter-test`).
- Added `?createDatabaseIfNotExist=true` to the datasource URL (same MySQL/XAMPP
  fix applied to `department-service` earlier).
- New `security`/`config` packages (JWT verification, mirrors `department-service`).
- `POST/PUT/DELETE /api/organizations/**` now require `ROLE_ADMIN`; `GET` is
  open to any authenticated caller.

### `department-service` (port 9003, MySQL `department_db`)

- New `security`/`config` packages for JWT verification.
- `POST/PUT/DELETE /api/departments/**` now require `ROLE_ADMIN`; `GET` is open
  to any authenticated caller.
- **New: departments now belong to an organization.** `Department`/`DepartmentDto`
  gained an `organizationId` field. On create/update, `DirectoryClient` calls
  `GET {organization-service}/api/organizations/{organizationId}` and rejects
  the request if it 404s.

### `employee-service` (port 9004, H2 in-memory)

- **Fixed a dormant integration bug**: the controller was mapped to
  `/api/employees`, but `leave-servcie` was already calling
  `/api/v1/employees/{id}` to enrich leave details — every such call was
  silently 404ing. Controller path is now `/api/v1/employees` to match.
- **New: employees now reference a real department and organization.**
  `Employee.department` (a free-text `String`) was replaced with
  `departmentId` + `organizationId` (`Long` FKs). On create/update,
  `DirectoryClient` validates both against `department-service` and
  `organization` via `WebClient` before saving.
- New `security`/`config` packages for JWT verification.
- `POST/PUT/DELETE /api/v1/employees/**` now require `ROLE_ADMIN`; `GET` is
  open to any authenticated caller. H2 console (`/h2-console`) is left open
  for local development, as it was before.

### `leave-servcie` (port 9006, H2 in-memory)

- **Fixed a broken build**: same fake-artifact / non-existent parent version
  problem as `organization`'s `pom.xml`; also `java.version` was `17` while
  every other service targets `21`. Fixed to parent `3.5.16`, real artifacts,
  Java `21`.
- **Fixed wrong hardcoded URLs**: `services.employee-service.url` pointed at
  `:8081` (actual port is `9004`); `services.notification-service.url` pointed
  at `:8084` (actual port is `9005`). Both corrected.
- New `security`/`config` packages for JWT verification.
- `PUT /api/v1/leaves/{id}/status` (approve/reject) now requires `ROLE_ADMIN`.
  Applying for leave and reading leave records is open to any authenticated
  caller.
- `LeaveServiceImpl` now forwards the caller's `Authorization` header when it
  calls `employee-service` (required now that `employee-service` enforces auth).

## 4. Config that must stay in sync

`app.jwt.secret` appears in **all six** services' `application.properties` /
`application.yaml` and must be identical everywhere, or token verification
will fail with a generic 401 on every downstream service while `user-service`
itself works fine. This is the single biggest thing to check if something
that used to return 200 suddenly returns 401 after these changes.

| Service | Port | Datastore |
|---|---|---|
| user-service | 9001 | Postgres `userservice` |
| organization | 9002 | MySQL `organization_db` |
| department-service | 9003 | MySQL `department_db` |
| employee-service | 9004 | H2 in-memory |
| notification-service | 9005 | (unchanged in this pass) |
| leave-servcie | 9006 | H2 in-memory |

## 5. End-to-end test walkthrough

Start services in this order (each depends on the previous one being up for
its own validation calls): `user-service` → `organization` →
`department-service` → `employee-service` → `notification-service` →
`leave-servcie`.

```bash
# 1. Log in as the seeded admin
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
# -> copy the "accessToken" from the response into $ADMIN_TOKEN

# 2. Create an organization (ADMIN only)
curl -X POST http://localhost:9002/api/organizations \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp","address":"1 Main St","email":"hr@acme.com","phone":"555-0100","website":"acme.com"}'
# -> note the returned "id", e.g. 1

# 3. Create a department under that organization (ADMIN only)
curl -X POST http://localhost:9003/api/departments \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"departmentName":"Engineering","departmentCode":"ENG","departmentAddress":"Floor 2","organizationId":1}'
# -> note the returned "id", e.g. 1

# 4. Create an employee in that department/organization (ADMIN only)
curl -X POST http://localhost:9004/api/v1/employees \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","position":"Backend Dev","departmentId":1,"organizationId":1,"salary":"50000"}'

# 5. Register a regular employee account and try an admin-only action
curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","email":"jane@acme.com","password":"Passw0rd!"}'
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" -d '{"username":"jane","password":"Passw0rd!"}'
# -> copy "accessToken" into $EMPLOYEE_TOKEN

curl -X POST http://localhost:9003/api/departments \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" -H "Content-Type: application/json" \
  -d '{"departmentName":"Sales","departmentCode":"SAL","departmentAddress":"Floor 1","organizationId":1}'
# -> expect HTTP 403 Forbidden

# 6. Apply for leave as the employee (allowed for any authenticated user)
curl -X POST http://localhost:9006/api/v1/leaves \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" -H "Content-Type: application/json" \
  -d '{"employeeId":1,"leaveType":"ANNUAL","startDate":"2026-08-01","endDate":"2026-08-05","reason":"Vacation"}'
# -> note the returned "id"

# 7. Employee tries to approve their own leave -> expect 403
curl -X PUT "http://localhost:9006/api/v1/leaves/1/status?status=APPROVED" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN"

# 8. Admin approves it -> expect 200
curl -X PUT "http://localhost:9006/api/v1/leaves/1/status?status=APPROVED" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 6. Known simplifications, and good next exercises for students

1. **No API gateway.** Every service duplicates the JWT-verification filter,
   and every service-to-service call has to manually forward the
   `Authorization` header. *Exercise: introduce Spring Cloud Gateway in front
   of everything, move JWT verification there once, and delete the duplicated
   `security` packages from the downstream services.*
2. **No service discovery.** URLs are hardcoded per service, which is exactly
   how the `leave-servcie` port bugs (§3) happened in the first place.
   *Exercise: add Eureka and switch `WebClient` calls to use logical service
   names instead of `localhost:PORT`.*
3. **The secret is copy-pasted six times.** Rotating it means editing six
   files and restarting every service in the right order. *Exercise: pull
   `app.jwt.secret` from a Spring Cloud Config server or an environment
   variable injected uniformly.*
4. **No department-scoped approval.** Any `ROLE_ADMIN` can approve leave for
   any employee, not just their own department. *Exercise: add a `MANAGER`
   role tied to a `departmentId` and scope `PUT /leaves/{id}/status` to
   managers of that employee's department (or admins).*
5. **`leave-servcie`'s enriched employee data is partly stale.** Its
   `EmployeeDto` still expects `email`/`department` (string) fields that
   `employee-service` no longer returns (it now returns `departmentId`/
   `organizationId`). Those fields will just come back `null` — not a crash,
   but a good exercise in why cross-service DTOs need to be kept in sync, or
   why you'd rather return `departmentId` everywhere and resolve names on the
   frontend.
6. **A user's role in an already-issued token is fixed until it expires**
   (access tokens live 15 minutes, per `app.jwt.accessExpirationMs`). If you
   promote someone to `ROLE_ADMIN` while they're logged in, they need to log
   in again (or wait for a refresh) to get a token with the new role.

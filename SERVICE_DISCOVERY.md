# Eureka + API Gateway — Teaching Notes

This is the follow-up to [RBAC_AND_SERVICE_INTEGRATION.md](RBAC_AND_SERVICE_INTEGRATION.md),
which explicitly called out "no API gateway" and "no service discovery" as
known simplifications (§6, exercises 1 and 2). This is that exercise: two new
standalone Maven projects — `eureka-server` and `api-gateway` — plus every
existing service now registers with Eureka. `attendance-service` and
`payroll-services` (this batch's two new student submissions) are wired in
too.

**What this does not change:** JWT verification still happens in each
service individually, and the two services that already called another
service directly (`department-service`/`employee-service`'s
`DirectoryClient`, `leave-servcie`'s `LeaveServiceImpl`) still use hardcoded
`localhost:PORT` URLs rather than Eureka names. That conversion, and moving
JWT verification into the gateway, are follow-up exercises — see the updated
§6 in the RBAC doc.

## 1. What each new piece is

- **`eureka-server`** — the service registry. Every other service tells it
  "I'm `employee-service`, I'm alive, here's my address" every 30 seconds.
  Nothing calls this directly except the other services' Eureka clients; you
  mostly interact with it through its dashboard.
- **`api-gateway`** — a Spring Cloud Gateway (reactive) app that is the one
  entry point on port `9080`. It looks up each service's address from Eureka
  at request time (`lb://service-name` in its routes) instead of having
  ports baked in. Routing only — it does not touch the `Authorization`
  header or verify anything; each service downstream still does that itself.

Both are ordinary standalone Maven projects (same shape as every other
service here, no multi-module parent pom), so they start with `mvnw
spring-boot:run` exactly like the rest.

## 2. Ports and Eureka names

`spring.application.name` is what shows up in the Eureka dashboard and what
the gateway's `lb://` routes and Feign clients refer to — it does not always
match the folder name (`leave-servcie` registers as `leave-service`,
`payroll-services` registers as `payroll-service`).

| Service | Folder | Eureka name | Port |
|---|---|---|---|
| Eureka server | `eureka-server` | `eureka-server` | 8761 |
| API Gateway | `api-gateway` | `api-gateway` | 9080 |
| User | `user-service` | `user-service` | 9001 |
| Organization | `organization` | `organization` | 9002 |
| Department | `department-service` | `department-service` | 9003 |
| Employee | `employee-service` | `employee-service` | 9004 |
| Notification | `notification-service` | `notification-service` | 9005 |
| Leave | `leave-servcie` | `leave-service` | 9006 |
| Payroll | `payroll-services` | `payroll-service` | 9008 |
| Attendance | `attendance-service` | `attendance-service` | 9007 |

Every service still binds its own fixed port too (so direct `localhost:PORT`
calls, curl examples in the RBAC doc, and Swagger UIs all keep working
exactly as before) — Eureka registration is additive, not a replacement.

## 3. Startup order for local run

1. **`eureka-server`** first — everything else's Eureka client will retry
   quietly if it's not up yet, but starting it first avoids the noisy
   `DiscoveryClient_*: registration failed` logs while it's catching up.
2. **Everything else, any order.** Each service is still fully independent
   otherwise (own database, own port). Give each one 10-30 seconds after
   startup to complete its first registration heartbeat.
3. **`api-gateway`** — start any time; it can come up before the services it
   routes to; requests through it will just 503 until the target service has
   registered.

```bash
cd eureka-server && ./mvnw spring-boot:run
# wait for "Started EurekaServerApplication" — then in separate terminals:
cd user-service && ./mvnw spring-boot:run
cd organization && ./mvnw spring-boot:run
cd department-service && ./mvnw spring-boot:run
cd employee-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
cd leave-servcie && ./mvnw spring-boot:run
cd payroll-services && ./mvnw spring-boot:run
cd attendance-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

(Windows without a shell that understands `./mvnw`: use `mvnw.cmd` instead,
or `mvn spring-boot:run` if you have Maven installed globally — both work,
every module has its own wrapper.)

## 4. How to verify it's working

**Eureka dashboard** — open `http://localhost:8761` in a browser once
`eureka-server` and a couple of other services are up. Each registered
service appears under "Instances currently registered with Eureka" using its
Eureka name from the table above (all-caps by convention, e.g.
`EMPLOYEE-SERVICE`).

**Gateway routes** — confirm the gateway loaded all 8 routes:

```bash
curl http://localhost:9080/actuator/gateway/routes
```

**A request through the gateway vs. direct** — these should return the same
thing once `employee-service` is registered and has at least one employee
(see the RBAC doc's end-to-end walkthrough for creating one and getting a
token):

```bash
# direct, bypassing the gateway
curl http://localhost:9004/api/v1/employees/1 -H "Authorization: Bearer $ADMIN_TOKEN"

# through the gateway on 9080 - same path, gateway resolves employee-service via Eureka
curl http://localhost:9080/api/v1/employees/1 -H "Authorization: Bearer $ADMIN_TOKEN"
```

If the gateway call 404s/503s while the direct call works, the most likely
cause is the service hasn't finished its first Eureka heartbeat yet — check
the dashboard.

## 5. What changed to connect `attendance-service` and `payroll-services`

Both already had comments in their `pom.xml`/`application.yml` anticipating
this step ("uncomment once Eureka Server is introduced") — those are now
uncommented rather than duplicated.

- **`payroll-services`'s `EmployeeClient`** used to point at a hardcoded,
  wrong URL (`${EMPLOYEE_SERVICE_URL:http://localhost:8081}` — actual port is
  `9004`) and called a path (`/api/employees/{id}/exists`) that
  `employee-service` never implemented. Both are fixed:
  - `EmployeeClient` no longer has a `url` attribute, so Feign resolves
    `employee-service` through Eureka + `spring-cloud-starter-loadbalancer`
    instead.
  - The path is now `/api/v1/employees/{id}/exists`, matching
    `employee-service`'s real base path, and `employee-service` now actually
    has that endpoint (`EmployeeController.employeeExists`,
    `EmployeeService.existsById`).
  - That endpoint is deliberately left unauthenticated
    (`SecurityConfig.filterChain` permits `/api/v1/employees/*/exists`)
    because `payroll-services` doesn't forward a caller JWT when it calls
    out — see the note below.
- **`attendance-service`** doesn't call any other service today, so
  "connected" for it currently just means registered with Eureka and routed
  through the gateway at `/api/attendance/**`. No client code changed.

## 6. A gap this did *not* fix — two auth models

`attendance-service` and `payroll-services` use `httpBasic` with hardcoded
`InMemoryUserDetailsManager` users and roles like `HR_ADMIN`/`FINANCE`. Every
other service in this system uses the shared JWT scheme from the RBAC doc
(`ROLE_ADMIN`/`ROLE_EMPLOYEE`, same `app.jwt.secret`). These two are not part
of that trust fabric — a token from `user-service` won't authenticate
against either of them, and neither can be a caller that forwards a JWT
somewhere else meaningfully.

This wasn't touched here because it's a bigger design decision (should they
adopt the shared JWT/role scheme, and if so which roles map to
`HR_ADMIN`/`FINANCE`?) than "make services discoverable." Worth raising with
your student and deciding deliberately, rather than folding it into
infrastructure work.

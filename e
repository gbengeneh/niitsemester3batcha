import urllib.request, json, base64, random

results = []

def test(name, url, method="GET", data=None, headers=None, auth=None):
    try:
        req = urllib.request.Request(url, method=method)
        if headers:
            for k, v in headers.items():
                req.add_header(k, v)
        if data:
            req.add_header("Content-Type", "application/json")
            req.data = json.dumps(data).encode()
        if auth:
            enc = base64.b64encode(f"{auth[0]}:{auth[1]}".encode()).decode()
            req.add_header("Authorization", f"Basic {enc}")
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode()
        results.append((name, resp.status, "PASS"))
        print(f"[{resp.status}] {name}")
        return body
    except urllib.error.HTTPError as e:
        body = e.read().decode()[:200]
        ok = "PASS" if ("expect 403" in name.lower() and e.code == 403) else "FAIL"
        results.append((name, e.code, ok))
        print(f"[{e.code}] {name} - {ok}")
        if body.strip() and ok == "FAIL":
            print(f"  {body[:150]}")
        return body
    except Exception as e:
        results.append((name, 0, "ERROR"))
        print(f"[ERR] {name} - {e}")
        return None

r = random.randint(10000, 99999)
print("=" * 55)
print("  E2E TEST: ALL MICROSERVICES")
print("=" * 55)

# 1 - Login
print("\n--- 1. LOGIN ---")
body = test("admin login", "http://localhost:9001/api/auth/login", "POST",
     {"username": "admin", "password": "Admin@123"})
if not body:
    print("  user-service not responding. Exiting.")
    exit(1)
j = json.loads(body)
token = j["accessToken"]
print(f"  user: {j['username']}  roles: {[x['name'] for x in j['roles']]}")
h = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

# 2 - Gateway
print("\n--- 2. GATEWAY ---")
test("gateway health", "http://localhost:9080/actuator/health")

# 3 - Organization
print("\n--- 3. ORGANIZATION ---")
test("GET /api/organizations", "http://localhost:9080/api/organizations", headers=h)
body = test("POST /api/organizations", "http://localhost:9080/api/organizations", "POST",
    {"name": f"Org{r}", "address": "1 St", "email": f"o{r}@t.com", "phone": "555", "website": "o.com"}, h)
oid = json.loads(body)["id"]

# 4 - Department
print("\n--- 4. DEPARTMENT ---")
body = test("POST /api/departments", "http://localhost:9080/api/departments", "POST",
    {"departmentName": "Engineering", "departmentCode": "ENG", "departmentAddress": "F2", "organizationId": oid}, h)
did = json.loads(body)["id"]

# 5 - Employee
print("\n--- 5. EMPLOYEE ---")
body = test("POST /api/v1/employees", "http://localhost:9080/api/v1/employees", "POST",
    {"name": "Jane", "position": "Dev", "departmentId": did, "organizationId": oid, "salary": "60000"}, h)
eid = json.loads(body)["id"]
if eid:
    test("GET /api/v1/employees/{id}/exists (no auth)", f"http://localhost:9080/api/v1/employees/{eid}/exists")

# 6 - Leave
print("\n--- 6. LEAVE ---")
test("POST /api/v1/leaves", "http://localhost:9080/api/v1/leaves", "POST",
    {"employeeId": eid, "leaveType": "ANNUAL", "startDate": "2026-09-01", "endDate": "2026-09-05", "reason": "Vacation"}, h)

# 7 - Notification
print("\n--- 7. NOTIFICATION (no auth) ---")
test("POST /api/v1/notifications/email", "http://localhost:9080/api/v1/notifications/email", "POST",
    {"to": "a@b.com", "subject": "T", "body": "H"})

# 8 - Payroll
print("\n--- 8. PAYROLL (basic auth) ---")
test("POST /api/payroll", "http://localhost:9080/api/payroll", "POST",
    {"employeeId": eid, "payPeriod": "2026-10", "basicSalary": 60000, "allowances": 5000, "deductions": 2000, "grossPay": 65000, "netPay": 63000},
    auth=("admin", "admin"))
test("GET /api/payroll/{id}", f"http://localhost:9080/api/payroll/{eid}", auth=("admin", "admin"))

# 9 - Attendance
print("\n--- 9. ATTENDANCE ---")
try:
    urllib.request.urlopen(urllib.request.Request("http://localhost:9007/api/attendance"), timeout=3)
    print("[200] attendance reachable")
except urllib.error.HTTPError as e:
    print(f"[{e.code}] attendance needs httpBasic (expected)")

# 10 - RBAC
print("\n--- 10. RBAC ---")
body = test("register non-admin", "http://localhost:9001/api/auth/register", "POST",
    {"username": f"u{r}", "email": f"u{r}@t.com", "password": "Passw0rd!"})
et = json.loads(body)["accessToken"] if body else ""
if et:
    test("non-admin CREATE dept (expect 403)", "http://localhost:9080/api/departments", "POST",
        {"departmentName": "Sales", "departmentCode": "SAL", "departmentAddress": "F1", "organizationId": oid},
        {"Authorization": f"Bearer {et}", "Content-Type": "application/json"})

# 11 - User Mgmt
print("\n--- 11. USER MANAGEMENT ---")
test("GET /api/auth/users", "http://localhost:9080/api/auth/users", headers=h)

# Summary
p = sum(1 for *_, s in results if s == "PASS")
f = sum(1 for *_, s in results if s == "FAIL")
print("\n" + "=" * 55)
print(f"  PASS: {p}/{len(results)}  |  FAIL: {f}/{len(results)}")
print("=" * 55)
for n, c, s in results:
    sym = {"PASS": "OK", "FAIL": "XX"}[s]
    print(f"  {sym} [{c}] {n}")

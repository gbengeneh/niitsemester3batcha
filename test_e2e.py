import urllib.request, json, base64

results = []

def test(name, url, method="GET", data=None, headers=None, auth=None):
    try:
        req = urllib.request.Request(url, method=method)
        if headers:
            for k, v in headers.items():
                req.add_header(k, v)
        if data:
            req.add_header("Content-Type", "application/json")
            req.data = json.dumps(data).encode("utf-8")
        if auth:
            encoded = base64.b64encode(f"{auth[0]}:{auth[1]}".encode()).decode()
            req.add_header("Authorization", f"Basic {encoded}")
        
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        status = resp.status
        results.append((name, status, "PASS", body[:100]))
        print(f"[{status}] {name} - PASS")
        if body:
            print(f"  {body[:150]}")
        return body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        status = e.code
        r = "PASS" if status < 400 else "FAIL"
        results.append((name, status, r, body[:100]))
        print(f"[{status}] {name} - {r}")
        print(f"  ERROR BODY: {body[:500]}")
        return body
    except Exception as e:
        results.append((name, 0, "ERROR", str(e)))
        print(f"[ERR]  {name} - {e}")
        return None

print("=" * 60)
print("  MICROSERVICES ENDPOINT TEST")
print("=" * 60)

# 1 - Login
print("\n--- 1. AUTH ---")
body = test("Admin Login", "http://localhost:9001/api/auth/login", "POST",
            {"username": "admin", "password": "Admin@123"})
token = json.loads(body).get("accessToken", "") if body else ""
hdr = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
print(f"  Token: {token[:40]}...")

# 2 - Gateway
print("\n--- 2. GATEWAY ---")
test("GW Health", "http://localhost:9080/actuator/health")

# 3 - Organization
print("\n--- 3. ORGANIZATION ---")
test("GET Orgs", "http://localhost:9080/api/organizations", headers=hdr)
body = test("POST Org", "http://localhost:9080/api/organizations", "POST",
    {"name":"Acme Corp","address":"1 Main St","email":"hr@acme.com","phone":"555-0100","website":"acme.com"}, hdr)
org_id = json.loads(body).get("id") if body else None
print(f"  Org ID: {org_id}")

# 4 - Department
print("\n--- 4. DEPARTMENT ---")
body = test("POST Dept", "http://localhost:9080/api/departments", "POST",
    {"departmentName":"Engineering","departmentCode":"ENG","departmentAddress":"Floor 2","organizationId":org_id or 1}, hdr)
dept_id = json.loads(body).get("id") if body else None
print(f"  Dept ID: {dept_id}")

# 5 - Employee
print("\n--- 5. EMPLOYEE ---")
body = test("POST Emp", "http://localhost:9080/api/v1/employees", "POST",
    {"name":"Jane Doe","position":"Backend Dev","departmentId":dept_id or 1,"organizationId":org_id or 1,"salary":"50000"}, hdr)
emp_id = json.loads(body).get("id") if body else None
print(f"  Emp ID: {emp_id}")

if emp_id:
    test(f"Emp Exists {emp_id}", f"http://localhost:9080/api/v1/employees/{emp_id}/exists")

# 6 - Leave
print("\n--- 6. LEAVE ---")
body = test("POST Leave", "http://localhost:9080/api/v1/leaves", "POST",
    {"employeeId":emp_id or 1,"leaveType":"ANNUAL","startDate":"2026-08-01","endDate":"2026-08-05","reason":"Vacation"}, hdr)
leave_id = json.loads(body).get("id") if body else None
print(f"  Leave ID: {leave_id}")

if leave_id:
    test(f"APPROVE Leave", f"http://localhost:9080/api/v1/leaves/{leave_id}/status?status=APPROVED", "PUT", headers=hdr)

# 7 - Notification
print("\n--- 7. NOTIFICATION ---")
test("POST Email", "http://localhost:9080/api/v1/notifications/email", "POST",
    {"to":"user@test.com","subject":"Test","body":"Hello"})

# 8 - Payroll
print("\n--- 8. PAYROLL ---")
test("POST Payroll", "http://localhost:9080/api/payroll", "POST",
    {"employeeId":emp_id or 1,"payPeriod":"2026-07","basicSalary":50000.0,"allowances":5000.0,"deductions":2000.0,"grossPay":55000.0,"netPay":53000.0},
    auth=("admin","admin"))

if emp_id:
    test(f"GET Payroll {emp_id}", f"http://localhost:9080/api/payroll/{emp_id}", auth=("admin","admin"))

# 9 - Attendance
print("\n--- 9. ATTENDANCE ---")
try:
    urllib.request.urlopen("http://localhost:9007", timeout=3)
    print("  [200] Attendance root reachable")
except urllib.error.HTTPError as e:
    print(f"  [{e.code}] Attendance reachable (needs auth - expected)")
except Exception as e:
    print(f"  [ERR] Attendance - {e}")

# 10 - RBAC
print("\n--- 10. RBAC ---")
body = test("Register employee", "http://localhost:9001/api/auth/register", "POST",
    {"username":"rbacuser","email":"rbac@test.com","password":"Passw0rd!"})
emp_token = json.loads(body).get("accessToken","") if body else ""
if emp_token:
    test("EMP tries CREATE Dept (expect 403)", "http://localhost:9080/api/departments", "POST",
        {"departmentName":"Sales","departmentCode":"SAL","departmentAddress":"Floor 1","organizationId":org_id or 1},
        {"Authorization": f"Bearer {emp_token}", "Content-Type": "application/json"})

# Summary
print("\n" + "=" * 60)
print("  RESULTS SUMMARY")
print("=" * 60)
for name, status, r, _ in results:
    icon = {"PASS":"✓","FAIL":"✗","ERROR":"!"}.get(r, "?")
    print(f"  {icon} [{status}] {name}")

total = len(results)
passed = sum(1 for *_, r in results if r == "PASS")
failed = sum(1 for *_, r in results if r == "FAIL")
print(f"\n  Pass: {passed}/{total}  |  Fail: {failed}/{total}  |  Total: {total}")
print("=" * 60)

# Introduction to JUnit Testing: Employee Service

This unit introduces automated testing with **JUnit 5**, **Mockito**, Spring Boot Test, and MockMvc. All examples focus only on `employee-service`.

## Learning outcomes

By the end of the lesson, a student should be able to:

1. explain why automated tests are useful;
2. write a JUnit 5 test using Arrange, Act, Assert;
3. use assertions to verify an expected result;
4. use Mockito to isolate a class from its dependencies;
5. distinguish a unit test from an integration test;
6. test an HTTP endpoint with MockMvc; and
7. run the employee-service test suite with Maven.

## 1. What is testing?

Testing checks that software behaves as expected. A test supplies an input, runs part of the application, and compares the actual result with the expected result.

Automated tests help us:

- find defects early;
- change code with more confidence;
- document expected behaviour;
- prevent an old defect from returning (a regression); and
- receive fast feedback without repeatedly testing every endpoint by hand.

Testing cannot prove that an application has no defects. Good tests reduce risk by checking important normal, boundary, and failure cases.

## 2. The testing pyramid

| Test type | Scope | Speed | Employee-service example |
|---|---|---:|---|
| Unit test | One class or method | Fastest | Test `EmployeeService` while mocking its repository and directory client |
| Integration test | Several real components working together | Slower | Send HTTP requests through the controller, service, JPA repository, and H2 database |
| End-to-end test | The complete deployed system | Slowest | Call employee-service with real department, organization, authentication, and database services |

This teaching unit implements the first two levels. End-to-end testing is described but is outside the employee-service-only scope.

## 3. JUnit 5 fundamentals

A basic test follows the **Arrange, Act, Assert (AAA)** pattern:

```java
@Test
void toDTO_copiesEmployeeFields() {
    // Arrange
    Employee employee = new Employee(1L, "Ada", "Engineer", 10L, 20L, "500000");

    // Act
    EmployeeDTO result = EmployeeMapper.toDTO(employee);

    // Assert
    assertEquals("Ada", result.getName());
}
```

Important JUnit features:

- `@Test` marks a test method.
- `@BeforeEach` runs before every test and is useful for fresh test data.
- `assertEquals(expected, actual)` compares values.
- `assertTrue(condition)` checks a boolean condition.
- `assertNotNull(value)` checks that a result exists.
- `assertThrows(Type.class, action)` verifies an expected exception.
- `assertAll(...)` groups related assertions so they are all evaluated.

Use descriptive test names such as `getEmployeeById_whenMissing_throwsResourceNotFoundException`. The name communicates the method, situation, and expected result.

## 4. General/basic test: EmployeeMapperTest

`EmployeeMapperTest` is a simple test with no Spring context and no mocks. It demonstrates:

- construction of input data;
- calling a method directly;
- several assertions; and
- testing conversion in both directions.

This is a good first test for beginners because the mapper has no external dependencies.

## 5. Unit testing EmployeeService with Mockito

A **unit test** tests one unit in isolation. `EmployeeService` depends on:

- `EmployeeRepository`, which normally accesses the database; and
- `DirectoryClient`, which normally calls other microservices.

The unit test replaces both dependencies with Mockito mocks:

```java
@Mock
private EmployeeRepository repository;

@Mock
private DirectoryClient directoryClient;

@InjectMocks
private EmployeeService employeeService;
```

Common Mockito operations:

- `when(repository.findById(1L)).thenReturn(Optional.of(employee))` defines mock behaviour.
- `verify(repository).save(any(Employee.class))` proves an interaction happened.
- `verifyNoInteractions(directoryClient)` proves no remote validation was attempted.
- `ArgumentCaptor<Employee>` captures a value sent to a dependency for closer inspection.

Do not mock the class being tested. Mock its boundaries and call the real method on the class under test.

The service unit tests cover:

- returning all employees;
- returning one employee;
- handling a missing employee;
- creating an employee and validating its directory references;
- updating the existing persisted entity;
- deleting an employee; and
- checking whether an employee exists.

## 6. Integration testing with Spring Boot and MockMvc

An **integration test** checks that several application components work together. `EmployeeControllerIntegrationTest` starts the Spring application context and uses:

- the real controller;
- the real service;
- the real Spring Data JPA repository;
- a real in-memory H2 test database;
- real validation and Spring Security configuration; and
- MockMvc to make HTTP requests without opening a network port.

Only `DirectoryClient` is mocked. Department-service and organization-service are outside the requested employee-service boundary. This makes the test repeatable and prevents it from requiring two separately running applications.

Examples of behaviour checked:

- an admin can create an employee;
- an employee without the admin role cannot create one;
- invalid JSON receives HTTP 400;
- a created employee can be retrieved from the API; and
- an unknown employee receives HTTP 404.

`@WithMockUser(roles = "ADMIN")` creates an authenticated test user. It does not create a production JWT; it supplies the Spring Security authentication needed to test authorization rules.

## 7. Test independence

Every test must be able to run alone and in any order.

- Unit tests create fresh objects in `@BeforeEach`.
- Integration tests call `repository.deleteAll()` before each test.
- The integration test uses an in-memory H2 database.
- No test should depend on an employee created by a previous test.

Avoid fixed database IDs in integration assertions because generated IDs may change.

## 8. Running the tests

From the repository root on Windows:

```powershell
cd employee-service
.\mvnw.cmd test
```

Run one class:

```powershell
.\mvnw.cmd -Dtest=EmployeeServiceTest test
```

Run one method:

```powershell
.\mvnw.cmd "-Dtest=EmployeeServiceTest#getEmployeeById_whenEmployeeExists_returnsDTO" test
```

Test reports are generated in `employee-service/target/surefire-reports`.

## 9. Reading a failed test

When a test fails:

1. read the first assertion failure or exception;
2. identify the expected and actual values;
3. open the named test method and application method;
4. decide whether the production code or the test expectation is wrong;
5. make one focused correction; and
6. run the test again, then run the full suite.

Never change an assertion only to make a red test green. First confirm the required behaviour.

## 10. Suggested classroom flow

1. Run the existing suite and identify the green baseline.
2. Read `EmployeeMapperTest`.
3. Temporarily change one expected value and inspect the failure message, then restore it.
4. Read `EmployeeServiceTest` and identify mocks, stubbing, the action, assertions, and verification.
5. Read `EmployeeControllerIntegrationTest` and trace a request from MockMvc to H2.
6. Add one new test independently.

## 11. Student exercises

### Exercise A — beginner

Add a mapper test proving that a `null` salary remains `null`.

### Exercise B — unit test

Add a test proving that `updateEmployee` throws `ResourceNotFoundException` when the ID does not exist. Verify that neither `DirectoryClient` nor `repository.save(...)` is called.

### Exercise C — integration test

Add an admin `PUT /api/v1/employees/{id}` test. Create an employee in the repository, update its name, and verify both the JSON response and stored entity.

### Exercise D — design discussion

At present, `deleteEmployee` directly calls `deleteById`. Discuss whether deleting an unknown ID should succeed silently or return HTTP 404. Write the test first, then change the production behaviour only after the class agrees on the requirement.

## 12. Review questions

1. Why is `DirectoryClient` mocked in the service unit test?
2. Why does an integration test usually run more slowly than a unit test?
3. What is the difference between `when(...)` and `verify(...)`?
4. Why should tests not depend on execution order?
5. What does HTTP 400 test that a direct service unit test does not?
6. Why do we test both a successful case and a failure case?

## File map

```text
src/test/java/com/semester3/employee_service/
├── EmployeeMapperTest.java
├── EmployeeServiceApplicationTests.java
├── EmployeeServiceTest.java
└── integration/
    └── EmployeeControllerIntegrationTest.java
```

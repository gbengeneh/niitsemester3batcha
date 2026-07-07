package microservice_semester3.leave_servcie.dto;

public class EmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String department;

    // No-Args Constructor
    public EmployeeDto() {}

    // All-Args Constructor
    public EmployeeDto(Long id, String name, String email, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

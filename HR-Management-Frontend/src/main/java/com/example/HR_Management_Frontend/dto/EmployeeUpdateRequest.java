package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO sent as the JSON body of a PUT /api/v1/employees/{id} request.
 *
 * Spring Data REST requires a complete replacement body for PUT — it does not
 * support PATCH semantics by default. We therefore populate ALL fields from
 * the existing EmployeeDTO and then overwrite only the editable ones
 * (email, phoneNumber, salary) before sending.
 *
 * Association fields (job, department, manager) are sent as URI strings
 * so SDR can resolve them back to entities. If they are null (employee has
 * no department, no manager, etc.) Jackson omits them via @JsonInclude.
 *
 * Field mapping to backend Employee entity:
 *   firstName, lastName, email, phoneNumber, hireDate, salary,
 *   commissionPct  → scalar fields (sent as-is)
 *   job            → URI link e.g. "http://localhost:8080/api/v1/jobs/SA_MAN"
 *   department     → URI link e.g. "http://localhost:8080/api/v1/department/80"
 *   manager        → URI link e.g. "http://localhost:8080/api/v1/employees/100"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeUpdateRequest {

    private BigDecimal employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private BigDecimal salary;
    private BigDecimal commissionPct;

    // Association URIs — SDR resolves these to entity references on the server
    private String job;
    private String department;
    private String manager;

    public EmployeeUpdateRequest() {}

    /**
     * Convenience factory: copies all scalar and association fields from an
     * existing EmployeeDTO so the PUT body is always complete.
     *
     * @param source      the current employee record fetched from the backend
     * @param backendUrl  the backend base URL (needed to build association URIs)
     */
    public static EmployeeUpdateRequest from(EmployeeDTO source, String backendUrl) {
        EmployeeUpdateRequest req = new EmployeeUpdateRequest();

        req.setEmployeeId(source.getEmployeeId());
        req.setFirstName(source.getFirstName());
        req.setLastName(source.getLastName());
        req.setEmail(source.getEmail());
        req.setPhoneNumber(source.getPhoneNumber());
        req.setHireDate(source.getHireDate());
        req.setSalary(source.getSalary());
        req.setCommissionPct(source.getCommissionPct());

        // Build association URI links from nested DTOs
        if (source.getJob() != null && source.getJob().getJobId() != null) {
            req.setJob(backendUrl + "/jobs/" + source.getJob().getJobId());
        }
        if (source.getDepartment() != null && source.getDepartment().getDepartmentId() != null) {
            req.setDepartment(backendUrl + "/department/" + source.getDepartment().getDepartmentId().toPlainString());
        }
        // Manager href is already a full URL in the HAL links; reuse it directly
        String managerHref = source.getManagerHref();
        if (managerHref != null && !managerHref.isBlank()) {
            req.setManager(managerHref);
        }

        return req;
    }

    public EmployeeUpdateRequest cleanedCopy(String backendUrl) {
        EmployeeUpdateRequest req = new EmployeeUpdateRequest();

        req.setEmployeeId(employeeId);
        req.setFirstName(clean(firstName));
        req.setLastName(clean(lastName));
        req.setEmail(clean(email));
        req.setPhoneNumber(clean(phoneNumber));
        req.setHireDate(hireDate);
        req.setSalary(salary);
        req.setCommissionPct(commissionPct);
        req.setJob(toUri(clean(job), backendUrl + "/jobs/"));
        req.setDepartment(toUri(clean(department), backendUrl + "/department/"));
        req.setManager(toUri(clean(manager), backendUrl + "/employees/"));

        return req;
    }

    public String validationError() {
        if (employeeId == null) {
            return "Employee ID is required.";
        }
        if (isBlank(firstName)) {
            return "First name is required.";
        }
        if (isBlank(lastName)) {
            return "Last name is required.";
        }
        if (isBlank(email) || !email.contains("@")) {
            return "A valid email address is required for new employees.";
        }
        if (hireDate == null) {
            return "Hire date is required.";
        }
        if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0) {
            return "Salary must be greater than zero.";
        }
        if (isBlank(job)) {
            return "Job ID is required.";
        }
        return null;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toUri(String value, String prefix) {
        if (value == null || value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("/api/")) {
            return value;
        }
        int apiIndex = prefix.indexOf("/api/");
        String relativePrefix = apiIndex >= 0 ? prefix.substring(apiIndex) : prefix;
        return relativePrefix + value;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public BigDecimal getEmployeeId() { return employeeId; }
    public void setEmployeeId(BigDecimal v) { this.employeeId = v; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { this.phoneNumber = v; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate v) { this.hireDate = v; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal v) { this.salary = v; }

    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal v) { this.commissionPct = v; }

    public String getJob() { return job; }
    public void setJob(String v) { this.job = v; }

    public String getDepartment() { return department; }
    public void setDepartment(String v) { this.department = v; }

    public String getManager() { return manager; }
    public void setManager(String v) { this.manager = v; }
}

package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for deserialising an Employee from the Spring Data REST HAL response.
 *
 * Spring Data REST inlines all non-association fields directly in the JSON body.
 * Association fields (job, department, manager) are serialised as nested objects
 * when they are eagerly fetched (or via projections), and also appear as links
 * in _links. We rely on the nested objects here; _links are only used for the
 * manager-chain traversal (see HalLinksDTO).
 *
 * Backend Employee entity fields (snake_case in DB, camelCase in JSON):
 *   employeeId, firstName, lastName, email, phoneNumber, hireDate,
 *   salary, commissionPct, job (→ JobDTO), department (→ DepartmentDTO)
 *
 * Note: Jackson maps JSON "employeeId" → field employeeId automatically.
 *       The backend BigDecimal employeeId serialises as a plain number in JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDTO {

    private BigDecimal employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private BigDecimal salary;
    private BigDecimal commissionPct;

    // Nested association objects (inlined by SDR when not lazy)
    private JobDTO job;
    private DepartmentDTO department;
    private EmployeeDTO manager;

    // HAL _links block — used to extract manager href and self href
    @JsonProperty("_links")
    private HalLinksDTO links;

    public EmployeeDTO() {}

    // ── Derived helpers ──────────────────────────────────────────────────────

    /** Full display name, never null. */
    public String getFullName() {
        String f = firstName != null ? firstName : "";
        String l = lastName  != null ? lastName  : "";
        return (f + " " + l).trim();
    }

    /** Initials (up to 2 chars) for avatar rendering. */
    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty()) ? String.valueOf(firstName.charAt(0)) : "";
        String l = (lastName  != null && !lastName.isEmpty())  ? String.valueOf(lastName.charAt(0))  : "";
        return (f + l).toUpperCase();
    }

    /**
     * Extracts the numeric ID string from the HAL self-link href.
     * e.g. http://localhost:8080/api/v1/employees/149{?projection} → "149"
     * Falls back to employeeId.toPlainString() if links are absent.
     */
    public String getSelfId() {
        if (employeeId != null) {
            return employeeId.toPlainString();
        }
        if (links != null && links.getSelf() != null) {
            String href = links.getSelf().getHref();
            if (href != null) {
                href = href.replaceAll("\\{.*}", "");           // strip {?projection}
                return href.substring(href.lastIndexOf('/') + 1);
            }
        }
        return "";
    }

    /**
     * Returns the href of the manager association link, or null if absent.
     * Used by EmployeeController to walk the reporting chain.
     */
    public String getManagerHref() {
        if (links == null) return null;
        HalLinksDTO.HalLink mgr = links.getManager();
        if (mgr == null) return null;
        // Strip templated suffix {?projection}
        String href = mgr.getHref();
        return href != null ? href.replaceAll("\\{.*}", "") : null;
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

    public JobDTO getJob() { return job; }
    public void setJob(JobDTO v) { this.job = v; }

    public DepartmentDTO getDepartment() { return department; }
    public void setDepartment(DepartmentDTO v) { this.department = v; }

    public EmployeeDTO getManager() { return manager; }
    public void setManager(EmployeeDTO v) { this.manager = v; }

    public HalLinksDTO getLinks() { return links; }
    public void setLinks(HalLinksDTO v) { this.links = v; }
}

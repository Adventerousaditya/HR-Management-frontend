package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDTO {

    private Long       employeeId;
    private String     firstName;
    private String     lastName;
    private String     email;
    private BigDecimal salary;
    private String     hireDate;
    private String     jobId;

    // Populated from job association endpoint
    private String jobTitle;

    @JsonProperty("_links")
    private Links links;

    public Long       getEmployeeId()  { return employeeId; }
    public void       setEmployeeId(Long v)  { this.employeeId = v; }

    public String     getFirstName()   { return firstName; }
    public void       setFirstName(String v)  { this.firstName = v; }

    public String     getLastName()    { return lastName; }
    public void       setLastName(String v)   { this.lastName = v; }

    public String     getEmail()       { return email; }
    public void       setEmail(String v)      { this.email = v; }

    public BigDecimal getSalary()      { return salary; }
    public void       setSalary(BigDecimal v) { this.salary = v; }

    public String     getHireDate()    { return hireDate; }
    public void       setHireDate(String v)   { this.hireDate = v; }

    public String     getJobId()       { return jobId; }
    public void       setJobId(String v)      { this.jobId = v; }

    public String     getJobTitle()    { return jobTitle; }
    public void       setJobTitle(String v)   { this.jobTitle = v; }

    /** "First Last" */
    public String getFullName() {
        String f = firstName != null ? firstName : "";
        String l = lastName  != null ? lastName  : "";
        return (f + " " + l).trim();
    }

    /** "$1,25,000" formatted */
    public String getSalaryFormatted() {
        if (salary == null) return "—";
        return "$" + String.format("%,d", salary.longValue());
    }

    public Links getLinks() { return links; }
    public void  setLinks(Links l) { this.links = l; }

    public Long extractId() {
        if (links == null || links.self == null) return null;
        try {
            String[] p = links.self.href.split("/");
            return Long.parseLong(p[p.length - 1]);
        } catch (Exception e) { return null; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;
        public HRef getSelf() { return self; }
        public void setSelf(HRef s) { this.self = s; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;
        public String getHref() { return href; }
        public void setHref(String h) { this.href = h; }
    }
}
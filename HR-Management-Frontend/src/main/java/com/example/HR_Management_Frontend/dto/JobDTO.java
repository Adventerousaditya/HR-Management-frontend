package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the nested "job" object returned by Spring Data REST
 * inside an Employee HAL response.
 *
 * Backend entity fields: jobId, jobTitle, minSalary, maxSalary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDTO {

    private String jobId;
    private String jobTitle;
    private java.math.BigDecimal minSalary;
    private java.math.BigDecimal maxSalary;

    public JobDTO() {}

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public java.math.BigDecimal getMinSalary() { return minSalary; }
    public void setMinSalary(java.math.BigDecimal v) { this.minSalary = v; }

    public java.math.BigDecimal getMaxSalary() { return maxSalary; }
    public void setMaxSalary(java.math.BigDecimal v) { this.maxSalary = v; }
}

package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the nested "department" object returned inside an Employee HAL response.
 *
 * Backend entity fields: departmentId, departmentName, location, manager
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentDTO {

    private java.math.BigDecimal departmentId;
    private String departmentName;
    private LocationDTO location;

    public DepartmentDTO() {}

    public java.math.BigDecimal getDepartmentId() { return departmentId; }
    public void setDepartmentId(java.math.BigDecimal v) { this.departmentId = v; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocationDTO getLocation() { return location; }
    public void setLocation(LocationDTO location) { this.location = location; }
    
    
}

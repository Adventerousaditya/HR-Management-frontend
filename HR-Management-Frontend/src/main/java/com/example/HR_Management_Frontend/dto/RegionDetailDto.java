package com.example.HR_Management_Frontend.dto;

import java.util.List;

public class RegionDetailDto {

    private Long regionId;
    private String regionName;

    private List<CountryDto> countries;
    private List<LocationDto> locations;
    private List<EmployeeSummaryDto> employees;

    private int activeLocations;
    private int activeEmployees;

    // getters & setters

    public static class CountryDto {
        private String countryId;
        private String countryName;

        public CountryDto(String countryId, String countryName) {
            this.countryId = countryId;
            this.countryName = countryName;
        }

        public String getCountryId() { return countryId; }
        public String getCountryName() { return countryName; }
    }

    public static class LocationDto {
        private Long locationId;
        private String city;
        private String streetAddress;
        private String postalCode;
        private String stateProvince;
        private String countryName;

        // getters & setters
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }

        public String getCountryName() { return countryName; }
        public void setCountryName(String countryName) { this.countryName = countryName; }
    }

    public static class EmployeeSummaryDto {
        private Long employeeId;
        private String fullName;
        private String email;
        private String jobTitle;
        private String departmentName;
        private double salary;

        // getters & setters
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }
    }

    // getters & setters for main class

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public List<CountryDto> getCountries() { return countries; }
    public void setCountries(List<CountryDto> countries) { this.countries = countries; }

    public List<LocationDto> getLocations() { return locations; }
    public void setLocations(List<LocationDto> locations) { this.locations = locations; }

    public List<EmployeeSummaryDto> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeSummaryDto> employees) { this.employees = employees; }

    public int getActiveLocations() { return activeLocations; }
    public void setActiveLocations(int activeLocations) { this.activeLocations = activeLocations; }

    public int getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(int activeEmployees) { this.activeEmployees = activeEmployees; }
}
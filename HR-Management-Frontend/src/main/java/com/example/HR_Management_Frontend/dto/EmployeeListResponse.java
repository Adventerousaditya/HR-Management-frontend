package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Deserialises the HAL collection response returned by Spring Data REST
 * for employee list endpoints.
 *
 * SDR wraps lists like this:
 * {
 *   "_embedded": {
 *     "employees": [ { ...EmployeeDTO... }, ... ]
 *   },
 *   "_links": { ... },
 *   "page": { ... }
 * }
 *
 * Usage in controller:
 *   EmployeeListResponse response = restTemplate.getForObject(url, EmployeeListResponse.class);
 *   List<EmployeeDTO> employees = response.getEmployees();
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeListResponse {

    @JsonProperty("_embedded")
    private Embedded embedded;

    public List<EmployeeDTO> getEmployees() {
        if (embedded == null) return Collections.emptyList();
        List<EmployeeDTO> list = embedded.getEmployees();
        return list != null ? list : Collections.emptyList();
    }

    public void setEmbedded(Embedded embedded) { this.embedded = embedded; }
    public Embedded getEmbedded()              { return embedded; }

    // ── Inner class ──────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Embedded {
        private List<EmployeeDTO> employees;

        public List<EmployeeDTO> getEmployees()            { return employees; }
        public void setEmployees(List<EmployeeDTO> list)   { this.employees = list; }
    }
}

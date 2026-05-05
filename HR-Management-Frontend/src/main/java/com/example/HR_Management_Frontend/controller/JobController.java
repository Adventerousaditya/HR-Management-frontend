package com.example.HR_Management_Frontend.controller;

import com.example.HR_Management_Frontend.dto.EmployeeDTO;
import com.example.HR_Management_Frontend.dto.JobDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private static final int PAGE_SIZE = 10;

    private final RestTemplate restTemplate;

    @Value("${backend.base-url:http://localhost:8080/api/v1}")
    private String backendUrl;

    public JobController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAGE 1 – Job list  GET /jobs?page=0&search=
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public String listJobs(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "")   String search,
            Model model
    ) {
        List<JobDTO> jobs      = Collections.emptyList();
        int         totalPages = 1;
        String      loadError  = null;

        try {
            String url = (search != null && !search.isBlank())
                    ? backendUrl
                        + "/jobs/search/findByJobTitleContainingIgnoreCase"
                        + "?title=" + encodeParam(search.trim())
                        + "&page=" + page + "&size=" + PAGE_SIZE
                    : backendUrl + "/jobs?page=" + page + "&size=" + PAGE_SIZE;

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);

            if (body != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> embedded = (Map<String, Object>) body.get("_embedded");
                if (embedded != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rawJobs =
                            (List<Map<String, Object>>) embedded.get("jobs");
                    if (rawJobs != null) {
                        jobs = new ArrayList<>();
                        for (Map<String, Object> raw : rawJobs) {
                            jobs.add(toJobDTO(raw));
                        }
                    }
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> pageInfo = (Map<String, Object>) body.get("page");
                if (pageInfo != null) {
                    int tp = ((Number) pageInfo.get("totalPages")).intValue();
                    totalPages = tp == 0 ? 1 : tp;
                }
            }
        } catch (Exception e) {
            loadError = "Could not load jobs: " + e.getMessage();
        }

        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        model.addAttribute("jobs",        jobs);
        model.addAttribute("search",      search);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext",     currentPage < totalPages - 1);
        model.addAttribute("error",       loadError);
        return "job/list";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAGE 2 – Employees for a job (WITH PAGINATION)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{jobId}/employees")
    public String jobEmployees(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        JobDTO job = null;
        List<EmployeeDTO> employees = new ArrayList<>();
        int totalPages = 1;
        long totalItems = 0;
        String loadError = null;

        // 1. Fetch job details for the header
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawJob = restTemplate.getForObject(
                    backendUrl + "/jobs/" + jobId, Map.class);
            if (rawJob != null) {
                job = toJobDTO(rawJob);
                if (job.getJobId() == null) job.setJobId(jobId);
            }
        } catch (Exception e) {
            loadError = "Job '" + jobId + "' not found: " + e.getMessage();
        }

        // 2. Fetch PAGED employees using the backend search endpoint
        try {
            // Using the 'byJob' path defined in EmployeeRepository
            String url = backendUrl + "/employees/search/byJob"
                    + "?jobId=" + encodeParam(jobId)
                    + "&page=" + page 
                    + "&size=" + size;

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);

            if (body != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> embedded = (Map<String, Object>) body.get("_embedded");
                if (embedded != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rawEmps = (List<Map<String, Object>>) embedded.get("employees");
                    if (rawEmps != null) {
                        for (Map<String, Object> raw : rawEmps) {
                            employees.add(toEmployeeDTO(raw));
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> pageInfo = (Map<String, Object>) body.get("page");
                if (pageInfo != null) {
                    totalPages = ((Number) pageInfo.get("totalPages")).intValue();
                    totalItems = ((Number) pageInfo.get("totalElements")).longValue();
                }
            }
        } catch (Exception e) {
            if (loadError == null) loadError = "Could not load employees: " + e.getMessage();
        }

        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        model.addAttribute("job", job);
        model.addAttribute("jobId", jobId);
        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", totalItems);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("error", loadError);

        return "job/employees";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String selfLinkLastSegment(Map<String, Object> raw) {
        try {
            Map<String, Object> links = (Map<String, Object>) raw.get("_links");
            if (links == null) return null;
            Map<String, Object> self = (Map<String, Object>) links.get("self");
            if (self == null) return null;
            String href = (String) self.get("href");
            if (href == null) return null;
            href = href.replaceAll("\\{.*}", "");
            return href.substring(href.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private JobDTO toJobDTO(Map<String, Object> raw) {
        JobDTO dto = new JobDTO();
        dto.setJobId((String) raw.get("jobId"));
        if (dto.getJobId() == null || dto.getJobId().isBlank()) {
            dto.setJobId(selfLinkLastSegment(raw));
        }
        dto.setJobTitle((String) raw.get("jobTitle"));
        if (raw.get("minSalary") != null)
            dto.setMinSalary(new BigDecimal(raw.get("minSalary").toString()));
        if (raw.get("maxSalary") != null)
            dto.setMaxSalary(new BigDecimal(raw.get("maxSalary").toString()));
        return dto;
    }

    private EmployeeDTO toEmployeeDTO(Map<String, Object> raw) {
        EmployeeDTO dto = new EmployeeDTO();
        if (raw.get("employeeId") != null) {
            try { dto.setEmployeeId(new BigDecimal(raw.get("employeeId").toString())); }
            catch (Exception ignored) {}
        }
        if (dto.getEmployeeId() == null) {
            String selfId = selfLinkLastSegment(raw);
            if (selfId != null) {
                try { dto.setEmployeeId(new BigDecimal(selfId)); }
                catch (Exception ignored) {}
            }
        }
        dto.setFirstName  ((String) raw.get("firstName"));
        dto.setLastName   ((String) raw.get("lastName"));
        dto.setEmail      ((String) raw.get("email"));
        dto.setPhoneNumber((String) raw.get("phoneNumber"));
        // Salary data is excluded from UI mapping if not needed
        if (raw.get("hireDate") != null) {
            try { dto.setHireDate(LocalDate.parse(raw.get("hireDate").toString())); }
            catch (Exception ignored) {}
        }
        return dto;
    }

    private String encodeParam(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
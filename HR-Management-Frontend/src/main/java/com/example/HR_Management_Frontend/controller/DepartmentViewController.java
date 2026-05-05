package com.example.HR_Management_Frontend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class DepartmentViewController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${backend.url}")
    private String backendUrl;

    private static final String DEPT_PATH = "/department";

   
    @GetMapping({ "/departments"})
    public String listDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            String url = backendUrl + DEPT_PATH
                    + "?page=" + page + "&size=" + size + "&sort=departmentId,asc";
            Map<String, Object> body = getMap(url);
            List<Map<String, Object>> departments = new ArrayList<>();
            int  totalPages    = 1;
            long totalElements = 0;

            if (body.containsKey("_embedded")) {
                Map<String, Object> emb = (Map<String, Object>) body.get("_embedded");
                if (emb.containsKey("department"))
                    departments = (List<Map<String, Object>>) emb.get("department");
            }
            if (body.containsKey("page")) {
                Map<String, Object> pg = (Map<String, Object>) body.get("page");
                totalPages    = toInt(pg.get("totalPages"));
                totalElements = toLong(pg.get("totalElements"));
            }
            for (Map<String, Object> d : departments) enrichDepartment(d);

            model.addAttribute("departments",   departments);
            model.addAttribute("currentPage",   page);
            model.addAttribute("totalPages",    totalPages);
            model.addAttribute("totalElements", totalElements);
            model.addAttribute("pageSize",      size);
            model.addAttribute("searchMode",    null);
            model.addAttribute("searchValue",   null);

        } catch (Exception e) {
            model.addAttribute("departments",   Collections.emptyList());
            model.addAttribute("currentPage",   0);
            model.addAttribute("totalPages",    0);
            model.addAttribute("totalElements", 0L);
            model.addAttribute("searchMode",    null);
            model.addAttribute("searchValue",   null);
            model.addAttribute("error", "Failed to load departments: " + e.getMessage());
        }
        return "department-list";
    }

   
    @GetMapping("/departments/search")
    public String search(
            @RequestParam String searchType,
            @RequestParam String query,
            Model model) {

        List<Map<String, Object>> departments = new ArrayList<>();

        try {
            switch (searchType) {

                case "id" -> {
                    try {
                        Map<String, Object> dept =
                                getMap(backendUrl + DEPT_PATH + "/" + query.trim());
                        enrichDepartment(dept);
                        departments.add(dept);
                    } catch (HttpClientErrorException.NotFound e) {
                        model.addAttribute("error",
                                "No department found with ID: " + query);
                    }
                }

                case "name" -> {
                    Map<String, Object> body = getMap(
                            backendUrl + DEPT_PATH + "/search/byName?name=" + query.trim());
                    if (body.containsKey("_embedded")) {
                        Map<String, Object> emb =
                                (Map<String, Object>) body.get("_embedded");
                        if (emb.containsKey("department"))
                            departments = (List<Map<String, Object>>) emb.get("department");
                    }
                    for (Map<String, Object> d : departments) enrichDepartment(d);
                }

               

                default -> model.addAttribute("error", "Invalid search type.");
            }

        } catch (Exception e) {
            model.addAttribute("error", "Search failed: " + e.getMessage());
        }

        model.addAttribute("departments",   departments);
        model.addAttribute("totalElements", (long) departments.size());
        model.addAttribute("searchMode",    searchType);
        model.addAttribute("searchValue",   query);
        model.addAttribute("currentPage",   0);
        model.addAttribute("totalPages",    1);
        model.addAttribute("pageSize",      10);

        return "department-list";
    }

  
    @GetMapping("/departments/{id}")
    public String viewDepartment(@PathVariable String id, Model model) {
        try {
            Map<String, Object> dept = getMap(backendUrl + DEPT_PATH + "/" + id);
            enrichDepartment(dept);
            model.addAttribute("department", dept);

            List<Map<String, Object>> employees = new ArrayList<>();
            try {
                Map<String, Object> empBody = getMap(
                        backendUrl + "/employees/search/byDepartment?departmentId="
                                + id + "&size=100");
                if (empBody.containsKey("_embedded")) {
                    Map<String, Object> emb =
                            (Map<String, Object>) empBody.get("_embedded");
                    for (Object val : emb.values()) {
                        if (val instanceof List) {
                            employees = (List<Map<String, Object>>) val;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                model.addAttribute("empError", ex.getMessage());
            }

            for (Map<String, Object> emp : employees) enrichEmployee(emp);

            Map<String, Object> highestPaid = null, lowestPaid = null;
            double maxSal = Double.MIN_VALUE, minSal = Double.MAX_VALUE;
            for (Map<String, Object> emp : employees) {
                Object s = emp.get("salary");
                if (s == null) continue;
                try {
                    double sal = Double.parseDouble(s.toString());
                    if (sal > maxSal) { maxSal = sal; highestPaid = emp; }
                    if (sal < minSal) { minSal = sal; lowestPaid  = emp; }
                } catch (NumberFormatException ignored) {}
            }

            model.addAttribute("employees",   employees);
            model.addAttribute("highestPaid", highestPaid);
            model.addAttribute("lowestPaid",  lowestPaid);

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load department: " + e.getMessage());
        }
        return "department-detail";
    }

  
    @GetMapping("/departments/new")
    public String showAddForm() {
        return "department-add";
    }

   
    @GetMapping("/departments/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        try {
            Map<String, Object> dept = getMap(backendUrl + DEPT_PATH + "/" + id);
            enrichDepartment(dept);
            model.addAttribute("department", dept);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load: " + e.getMessage());
        }
        return "department-edit";
    }

    
    @PostMapping("/departments/create")
    public String createDepartment(
            @RequestParam String departmentId,
            @RequestParam String departmentName,
            @RequestParam String locationId,
            @RequestParam(required = false) String managerId) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("departmentId",   new java.math.BigDecimal(departmentId));
            payload.put("departmentName", departmentName);
            payload.put("location",       backendUrl + "/location/" + locationId);
            if (managerId != null && !managerId.isBlank())
                payload.put("manager", backendUrl + "/employees/" + managerId);
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(backendUrl + DEPT_PATH,
                    new HttpEntity<>(payload, h), Map.class);
            return "redirect:/departments?success=Department+created+successfully";
        } catch (HttpClientErrorException e) {
            return "redirect:/departments/new?error=" + encode(e.getResponseBodyAsString());
        } catch (Exception e) {
            return "redirect:/departments/new?error=" + encode(e.getMessage());
        }
    }

   
    @PostMapping("/departments/{id}/update")
    public String updateDepartment(
            @PathVariable String id,
            @RequestParam String departmentName,
            @RequestParam String locationId) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("departmentName", departmentName);
            payload.put("location",       backendUrl + "/location/" + locationId);
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.parseMediaType("application/merge-patch+json"));
            restTemplate.exchange(backendUrl + DEPT_PATH + "/" + id,
                    HttpMethod.PATCH, new HttpEntity<>(payload, h), Map.class);
            return "redirect:/departments/" + id + "?success=Updated+successfully";
        } catch (Exception e) {
            return "redirect:/departments/" + id + "?error=" + encode(e.getMessage());
        }
    }

    

    @SuppressWarnings("unchecked")
    private void enrichDepartment(Map<String, Object> dept) {
        if (dept.get("departmentId") == null) {
            String selfId = extractSelfId(dept);
            if (selfId != null) dept.put("departmentId", selfId);
        }

        String locHref = extractLink(dept, "location");
        if (locHref != null) {
            try {
                Map<String, Object> loc = getMap(locHref);
                dept.put("locationCity",   nvl(loc.get("city")));
                dept.put("locationState",  nvl(loc.get("stateProvince")));
                dept.put("locationStreet", nvl(loc.get("streetAddress")));
                dept.put("locationPostal", nvl(loc.get("postalCode")));
                String locId = extractSelfId(loc);
                dept.put("locationId", locId != null ? locId : "");
            } catch (Exception ignored) {
                dept.put("locationCity", "");
                dept.put("locationState", "");
            }
        } else {
            Object locRaw = dept.get("location");
            if (locRaw instanceof Map) {
                Map<String, Object> loc = (Map<String, Object>) locRaw;
                dept.put("locationCity",   nvl(loc.get("city")));
                dept.put("locationState",  nvl(loc.get("stateProvince")));
                dept.put("locationStreet", nvl(loc.get("streetAddress")));
                dept.put("locationPostal", nvl(loc.get("postalCode")));
                dept.put("locationId",     nvl(loc.get("id")));
            } else {
                dept.put("locationCity",  "");
                dept.put("locationState", "");
            }
        }

        String mgrHref = extractLink(dept, "manager");
        if (mgrHref != null) {
            try {
                Map<String, Object> mgr = getMap(mgrHref);
                String mgrId = extractSelfId(mgr);
                dept.put("managerId",
                        mgrId != null ? mgrId : nvl(mgr.get("employeeId")));
                dept.put("managerName",
                        (nvl(mgr.get("firstName")) + " "
                                + nvl(mgr.get("lastName"))).trim());
            } catch (Exception ignored) {
                dept.put("managerId", "");
            }
        } else {
            dept.put("managerId", "");
        }
    }

    @SuppressWarnings("unchecked")
    private void enrichEmployee(Map<String, Object> emp) {
        String selfId = extractSelfId(emp);
        if (selfId != null) emp.put("employeeId", selfId);

        String jobHref = extractLink(emp, "job");
        if (jobHref != null) {
            try {
                Map<String, Object> job = getMap(jobHref);
                emp.put("jobTitle", nvl(job.get("jobTitle")));
            } catch (Exception ignored) {
                emp.put("jobTitle", "");
            }
        } else {
            Object jobRaw = emp.get("job");
            if (jobRaw instanceof Map)
                emp.put("jobTitle",
                        nvl(((Map<String, Object>) jobRaw).get("jobTitle")));
        }
        emp.put("fullName",
                (nvl(emp.get("firstName")) + " "
                        + nvl(emp.get("lastName"))).trim());
    }

    private Map<String, Object> getMap(String url) {
        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = resp.getBody();
        return body != null ? body : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private String extractSelfId(Map<String, Object> obj) {
        Object links = obj.get("_links");
        if (!(links instanceof Map)) return null;
        Object self = ((Map<String, Object>) links).get("self");
        if (!(self instanceof Map)) return null;
        String href = (String) ((Map<String, Object>) self).get("href");
        if (href == null) return null;
        if (href.contains("{")) href = href.substring(0, href.indexOf('{'));
        String[] parts = href.split("/");
        return parts[parts.length - 1];
    }

    @SuppressWarnings("unchecked")
    private String extractLink(Map<String, Object> obj, String rel) {
        Object links = obj.get("_links");
        if (!(links instanceof Map)) return null;
        Object linkObj = ((Map<String, Object>) links).get(rel);
        if (!(linkObj instanceof Map)) return null;
        String href = (String) ((Map<String, Object>) linkObj).get("href");
        if (href != null && href.contains("{"))
            href = href.substring(0, href.indexOf('{'));
        return href;
    }

    private String nvl(Object o)  { return o == null ? "" : o.toString(); }

    private int toInt(Object o) {
        try { return Integer.parseInt(o.toString()); }
        catch (Exception e) { return 1; }
    }

    private long toLong(Object o) {
        try { return Long.parseLong(o.toString()); }
        catch (Exception e) { return 0L; }
    }

    private String encode(String msg) {
        if (msg == null) return "Unknown+error";
        return msg.replace(" ", "+").replace("\"", "")
                  .replace("\n", "").replace("\r", "");
    }
}
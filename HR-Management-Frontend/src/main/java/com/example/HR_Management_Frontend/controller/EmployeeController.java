package com.example.HR_Management_Frontend.controller;

import com.example.HR_Management_Frontend.dto.EmployeeDTO;
import com.example.HR_Management_Frontend.dto.EmployeeListResponse;
import com.example.HR_Management_Frontend.dto.EmployeeUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final int PAGE_SIZE = 8;
    private static final int MAX_HIERARCHY_HOPS = 12;

    private final RestTemplate restTemplate;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${backend.base-url:http://localhost:8080/api/v1}")
    private String backendUrl;

    public EmployeeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String listManagers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String created,
            @RequestParam(required = false) String updated,
            @RequestParam(required = false) String updateError,
            Model model
    ) {
        List<EmployeeDTO> managers = Collections.emptyList();
        String loadError = null;

        try {
            EmployeeListResponse response = restTemplate.getForObject(
                    backendUrl + "/employees/search/managers",
                    EmployeeListResponse.class
            );
            managers = response != null ? response.getEmployees() : Collections.emptyList();
            managers.forEach(this::enrichEmployeeAssociations);
        } catch (Exception e) {
            loadError = "Could not fetch managers. Please make sure the backend is running on " + backendUrl + ".";
        }

        int totalManagers = managers.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalManagers / (double) PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int fromIndex = Math.min(currentPage * PAGE_SIZE, totalManagers);
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalManagers);

        model.addAttribute("managers", managers.subList(fromIndex, toIndex));
        model.addAttribute("totalManagers", totalManagers);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("newEmployee", new EmployeeUpdateRequest());
        String pageError = updateError != null
                ? "Could not update employee. If changing email, use a valid email address."
                : error;
        model.addAttribute("error", loadError != null ? loadError : pageError);
        model.addAttribute("created", created != null);
        model.addAttribute("updated", updated != null);

        return "managers";
    }

    @PostMapping
    public String createEmployee(
            @ModelAttribute EmployeeUpdateRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String validationError = request.validationError();
            if (validationError != null) {
                redirectAttributes.addAttribute("error", validationError);
                return "redirect:/employees";
            }

            EmployeeUpdateRequest cleaned = request.cleanedCopy(backendUrl);
            restTemplate.postForObject(backendUrl + "/employees", cleaned, EmployeeDTO.class);
            return "redirect:/employees?created=true";
        } catch (Exception e) {
            redirectAttributes.addAttribute(
                    "error",
                    "Could not create employee. Use a unique ID/email and valid Job, Department, and Manager IDs."
            );
            return "redirect:/employees";
        }
    }

    @PostMapping("/{id}/update")
    public String updateEmployeeFromManagersPage(
            @PathVariable String id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String salary
    ) {
        return updateEmployee(id, email, phoneNumber, salary, "/employees");
    }

    @GetMapping("/{id}")
    public String managerDetail(
            @PathVariable String id,
            @RequestParam(required = false) String updated,
            @RequestParam(required = false) String updateError,
            Model model
    ) {
        try {
            EmployeeDTO manager = fetchEmployee(id);
            EmployeeListResponse subResponse = restTemplate.getForObject(
                    backendUrl + "/employees/search/byManager?managerId=" + id,
                    EmployeeListResponse.class
            );

            List<EmployeeDTO> subordinates = subResponse != null
                    ? subResponse.getEmployees()
                    : Collections.emptyList();
            enrichEmployeeAssociations(manager);
            subordinates.forEach(this::enrichEmployeeAssociations);

            List<EmployeeDTO> hierarchy = buildHierarchy(id);
            hierarchy.forEach(this::enrichEmployeeAssociations);

            model.addAttribute("manager", manager);
            model.addAttribute("subordinates", subordinates);
            model.addAttribute("hierarchy", hierarchy);
            model.addAttribute("managerId", id);
            model.addAttribute("updated", updated != null);
            model.addAttribute("updateError", updateError != null);
            model.addAttribute("error", null);
        } catch (Exception e) {
            model.addAttribute("manager", null);
            model.addAttribute("subordinates", Collections.emptyList());
            model.addAttribute("hierarchy", Collections.emptyList());
            model.addAttribute("managerId", id);
            model.addAttribute("updated", false);
            model.addAttribute("updateError", updateError != null);
            model.addAttribute("error", "Could not load manager details for employee ID " + id + ".");
        }
        return "manager-detail";
    }

    @PostMapping("/{id}/detail/update")
    public String updateEmployeeFromDetailPage(
            @PathVariable String id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String salary
    ) {
        return updateEmployee(id, email, phoneNumber, salary, "/employees/" + id);
    }

    private String updateEmployee(
            String id,
            String email,
            String phoneNumber,
            String salary,
            String redirectBase
    ) {
        try {
            EmployeeDTO current = fetchEmployee(id);
            if (current == null) {
                return "redirect:" + redirectBase + "?updateError=true";
            }
            enrichEmployeeAssociations(current);

            Map<String, Object> patch = new LinkedHashMap<>();

            String submittedEmail = email != null ? email.trim() : null;
            boolean emailChanged = submittedEmail != null && !Objects.equals(submittedEmail, current.getEmail());
            if (emailChanged && !isValidEmail(submittedEmail)) {
                return "redirect:" + redirectBase + "?updateError=true";
            }

            if (emailChanged) {
                patch.put("email", submittedEmail);
            }
            if (phoneNumber != null && !Objects.equals(phoneNumber.trim(), current.getPhoneNumber())) {
                patch.put("phoneNumber", phoneNumber.trim());
            }
            if (salary != null && !salary.isBlank()) {
                BigDecimal newSalary = new BigDecimal(salary.trim());
                if (!sameAmount(newSalary, current.getSalary())) {
                    patch.put("salary", newSalary);
                }
            }

            if (!patch.isEmpty()) {
                if (!patch.containsKey("email") && !isValidEmail(current.getEmail())) {
                    patch.put("email", generatedValidEmail(current));
                }
                sendPatch(id, patch);
            }

            return "redirect:" + redirectBase + "?updated=true";
        } catch (Exception e) {
            return "redirect:" + redirectBase + "?updateError=true";
        }
    }

    private void sendPatch(String id, Map<String, Object> patch) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + "/employees/" + id))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(toJson(patch)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Backend update failed with status " + response.statusCode());
        }
    }

    private String toJson(Map<String, Object> values) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;

            json.append('"').append(escapeJson(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value instanceof Number) {
                json.append(value);
            } else {
                json.append('"').append(escapeJson(String.valueOf(value))).append('"');
            }
        }

        return json.append('}').toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isValidEmail(String value) {
        return value != null && value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private String generatedValidEmail(EmployeeDTO employee) {
        String id = employee.getSelfId();
        String name = employee.getFullName().toLowerCase().replaceAll("[^a-z0-9]+", ".");
        name = name.replaceAll("^\\.+|\\.+$", "");
        if (name.isBlank()) {
            name = "employee";
        }
        return name + "." + id + "@hr.com";
    }

    private boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }

    private List<EmployeeDTO> buildHierarchy(String employeeId) {
        List<EmployeeDTO> chain = new ArrayList<>();
        String currentId = employeeId;

        for (int hop = 0; hop < MAX_HIERARCHY_HOPS && currentId != null && !currentId.isBlank(); hop++) {
            EmployeeDTO current = fetchEmployeeWithManager(currentId);
            if (current == null) {
                break;
            }

            chain.add(0, current);

            EmployeeDTO manager = current.getManager();
            if (manager == null) {
                manager = fetchManagerAssociation(current.getSelfId());
            }
            enrichEmployeeAssociations(current);

            if (manager != null && manager.getEmployeeId() != null) {
                currentId = manager.getEmployeeId().toPlainString();
            } else {
                break;
            }

            if (current.getSelfId().equals(currentId)) {
                break;
            }
        }

        return chain;
    }

    private EmployeeDTO fetchEmployee(String id) {
        return restTemplate.getForObject(backendUrl + "/employees/" + id, EmployeeDTO.class);
    }

    private EmployeeDTO fetchEmployeeWithManager(String id) {
        try {
            return restTemplate.getForObject(
                    backendUrl + "/employees/search/withManager?id=" + id,
                    EmployeeDTO.class
            );
        } catch (Exception ignored) {
            return fetchEmployee(id);
        }
    }

    private EmployeeDTO fetchManagerAssociation(String employeeId) {
        try {
            return restTemplate.getForObject(
                    backendUrl + "/employees/" + employeeId + "/manager",
                    EmployeeDTO.class
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private void enrichEmployeeAssociations(EmployeeDTO employee) {
        if (employee == null || employee.getSelfId().isBlank()) {
            return;
        }

        if (employee.getJob() == null) {
            try {
                employee.setJob(restTemplate.getForObject(
                        backendUrl + "/employees/" + employee.getSelfId() + "/job",
                        com.example.HR_Management_Frontend.dto.JobDTO.class
                ));
            } catch (Exception ignored) {
                // Association may be absent for incomplete seed data.
            }
        }

        if (employee.getDepartment() == null) {
            try {
                employee.setDepartment(restTemplate.getForObject(
                        backendUrl + "/employees/" + employee.getSelfId() + "/department",
                        com.example.HR_Management_Frontend.dto.DepartmentDTO.class
                ));
            } catch (Exception ignored) {
                // Association is optional in the backend entity.
            }
        }
    }

}

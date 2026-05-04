package com.example.HR_Management_Frontend.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestTemplate;

import com.example.HR_Management_Frontend.dto.CountryDTO;

import java.util.List;
import java.util.Map;

@Controller
public class CountryController {

	@Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8089/api/v1";


    @GetMapping("/countries")
    public String listCountries( @ModelAttribute("message") String message,@RequestParam(defaultValue = "0") int page, Model model) {

        String url = BASE_URL + "/countries?projection=countryWithRegion&page=" + page + "&size=20";;
    	
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        Map embedded = (Map) response.getBody().get("_embedded");
        List<Map> countries = (List<Map>) embedded.get("countries");
        
        Map<String, Object> pageInfo = (Map<String, Object>) body.get("page");
        int totalPages = ((Number) pageInfo.get("totalPages")).intValue();

        model.addAttribute("countries", countries);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",totalPages);
    	
        return "country/list";
    }
    
    @GetMapping("/countries/{id}")
    public String countryDetail(@PathVariable String id, Model model) {

        // 1. Fetch country info with region name
        ResponseEntity<Map> countryResp = restTemplate.getForEntity(
            BASE_URL + "/countries/" + id + "?projection=countryWithRegion", Map.class);
        Map<String, Object> country = countryResp.getBody();

        // 2. Fetch employees by country ID
        ResponseEntity<Map> empResp = restTemplate.getForEntity(
            BASE_URL + "/employees/search/findByDepartment_Location_Country_CountryId?countryId=" + id, Map.class);
        Map empBody = empResp.getBody();
        Map embedded = (Map) empBody.get("_embedded");
        List<Map> employees = embedded != null ? (List<Map>) embedded.get("employees") : List.of();

        model.addAttribute("country", country);
        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", employees.size());

        return "country/detail";
    }
    
    
}
package com.example.HR_Management_Frontend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.HR_Management_Frontend.dto.CountryDTO;

import java.math.BigDecimal;
import java.util.*;

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


    @GetMapping("/countries/new")
    public String newCountryForm(Model model) {
        model.addAttribute("country", new CountryDTO());
        model.addAttribute("regions", fetchAllRegions());
        model.addAttribute("isEdit", false);
        return "country/form";
    }

        @GetMapping("/countries/{id}/edit")
    public String editCountryForm(@PathVariable String id, Model model) {
        CountryDTO form = fetchCountryAsForm(id);
        model.addAttribute("country", form);
        model.addAttribute("regions", fetchAllRegions());
        model.addAttribute("isEdit", true);
        return "country/form";
    }

    
    @PostMapping("/countries")
    public String addCountry(
            @Valid @ModelAttribute("country") CountryDTO country,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("regions", fetchAllRegions());
            model.addAttribute("isEdit", false);
            return "country/form";
        }

        
        Map<String, Object> regionBody = new LinkedHashMap<>();
        regionBody.put("regionId", country.getRegionId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("countryId",   country.getCountryId());
        body.put("countryName", country.getCountryName());
        body.put("region",      BASE_URL + "/regions/" + country.getRegionId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                BASE_URL + "/countries",
                new HttpEntity<>(body, headers),
                Void.class);

        String location = response.getHeaders().getLocation().toString();

        HttpHeaders regionHeaders = new HttpHeaders();
        regionHeaders.set("Content-Type", "text/uri-list");

        restTemplate.put(
                location + "/region",
                new HttpEntity<>(
                        BASE_URL + "/regions/" + country.getRegionId(),
                        regionHeaders
                )
        );

        ra.addFlashAttribute("message", "Country added successfully");
        return "redirect:/countries";
    }

    
    @PostMapping("/countries/{id}/update")
    public String updateCountry( @PathVariable String id, @Valid @ModelAttribute("country") CountryDTO country, BindingResult result, Model model, RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("regions", fetchAllRegions());
            model.addAttribute("isEdit", true);
            return "country/form";
        }

        
        Map<String, Object> regionBody = new LinkedHashMap<>();
        regionBody.put("regionId", country.getRegionId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("countryId",   id);
        body.put("countryName", country.getCountryName());
        body.put("region",      regionBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.put(
                BASE_URL + "/countries/" + id,
                new HttpEntity<>(body, headers));

        ra.addFlashAttribute("message", "Country updated successfully");
        return "redirect:/countries";
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

    
    private List<Map> fetchAllRegions() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                BASE_URL + "/regions", Map.class);
        Map embedded = (Map) response.getBody().get("_embedded");
        return (List<Map>) embedded.get("regions");
    }

    private CountryDTO fetchCountryAsForm(String id) {
        String url = BASE_URL + "/countries/" + id + "?projection=countryWithRegion";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> data = response.getBody();

        CountryDTO dto = new CountryDTO();
        dto.setCountryId((String) data.get("countryId"));
        dto.setCountryName((String) data.get("countryName"));

        Map<String, Object> region = (Map<String, Object>) data.get("region");
        if (region != null) {
            Object regionId = region.get("regionId");
            if (regionId instanceof Number) {
                dto.setRegionId(new BigDecimal(regionId.toString()));
            }
        }
        return dto;
    }
}
package com.example.HR_Management_Frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.HR_Management_Frontend.dto.CountryDTO;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CountryService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${backend.base-url}")
    private String baseUrl;

    // -------------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------------

    public Map getCountries(int page) {
        String url = baseUrl + "/countries?projection=countryWithRegion&page=" + page + "&size=20";
        return restTemplate.getForObject(url, Map.class);
    }

    // -------------------------------------------------------------------------
    // SINGLE
    // -------------------------------------------------------------------------

    public CountryDTO getCountryById(String id) {
    	String url = baseUrl + "/countries/" + id + "?projection=countryWithRegion";
        Map<String, Object> raw = restTemplate.getForObject(url, Map.class);

        CountryDTO dto = new CountryDTO();
        dto.setCountryId((String) raw.get("countryId"));
        dto.setCountryName((String) raw.get("countryName"));

        Map<String, Object> region = (Map<String, Object>) raw.get("region");
        if (region != null && region.get("regionId") != null) {
            Object regionIdRaw = region.get("regionId");
            // DEBUG — remove after fixing
            System.out.println("regionId raw value: " + regionIdRaw);
            System.out.println("regionId raw type: " + regionIdRaw.getClass().getName());
            dto.setRegionId(new BigDecimal(regionIdRaw.toString()));
        }

        return dto;
    }

    // -------------------------------------------------------------------------
    // SAVE (CREATE + UPDATE)
    // -------------------------------------------------------------------------

    public void saveCountry(CountryDTO dto) {
        Map<String, Object> payload = buildPayload(dto);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, jsonHeaders());

        if (dto.getCountryId() == null || dto.getCountryId().isBlank()) {
            restTemplate.postForObject(baseUrl + "/countries/manage", request, Map.class);
        } else {
            restTemplate.exchange(
                    baseUrl + "/countries/manage/" + dto.getCountryId(),
                    HttpMethod.PUT,
                    request,
                    Map.class);
        }
    }

    // -------------------------------------------------------------------------
    // REGIONS (for dropdown)
    // -------------------------------------------------------------------------

    public List<Map> getRegions() {
        String url = baseUrl + "/regions?size=100";
        Map response = restTemplate.getForObject(url, Map.class);
        Map embedded = (Map) response.get("_embedded");
        return embedded != null ? (List<Map>) embedded.get("regions") : List.of();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildPayload(CountryDTO dto) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("countryId",   dto.getCountryId());
        payload.put("countryName", dto.getCountryName());
        payload.put("regionId",    dto.getRegionId());
        return payload;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

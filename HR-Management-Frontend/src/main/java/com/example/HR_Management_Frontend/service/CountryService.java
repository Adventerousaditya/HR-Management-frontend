package com.example.HR_Management_Frontend.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.HR_Management_Frontend.dto.CountryDTO;

@Service
public class CountryService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${backend.base-url}")
    private String baseUrl;

    

    public Map getCountries(int page) {
        String url = baseUrl + "/countries?projection=countryWithRegion&page=" + page + "&size=20";
        return restTemplate.getForObject(url, Map.class);
    }

    

    public CountryDTO getCountryById(String id) {
    	String url = baseUrl + "/countries/" + id + "?projection=countryWithRegion";
        Map<String, Object> raw = restTemplate.getForObject(url, Map.class);

        CountryDTO dto = new CountryDTO();
        dto.setCountryId((String) raw.get("countryId"));
        dto.setCountryName((String) raw.get("countryName"));

        Map<String, Object> region = (Map<String, Object>) raw.get("region");
        if (region != null && region.get("regionId") != null) {
            Object regionIdRaw = region.get("regionId");
            
            dto.setRegionId(new BigDecimal(regionIdRaw.toString()));
        }

        return dto;
    }

    

    public void saveCountry(CountryDTO dto) {
        boolean exists = dto.getCountryId() != null
                && !dto.getCountryId().isBlank()
                && countryExists(dto.getCountryId());

        Map<String, Object> payload = buildPayload(dto);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, jsonHeaders());

        if (!exists) {
            restTemplate.postForObject(baseUrl + "/countries", request, Map.class);
        } else {
            restTemplate.exchange(
                    baseUrl + "/countries/" + dto.getCountryId(),
                    HttpMethod.PUT,
                    request,
                    Map.class);
            updateRegionAssociation(dto.getCountryId(), dto.getRegionId());
        }
    }

    private boolean countryExists(String id) {
        try {
            restTemplate.getForObject(baseUrl + "/countries/" + id, Map.class);
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    

    public List<Map> getRegions() {
        String url = baseUrl + "/regions?size=100";
        Map response = restTemplate.getForObject(url, Map.class);
        Map embedded = (Map) response.get("_embedded");
        List<Map> regions = embedded != null ? (List<Map>) embedded.get("regions") : List.of();

        for (Map r : regions) {
            Map links = (Map) r.get("_links");
            Map self  = links != null ? (Map) links.get("self") : null;
            String href = self != null ? (String) self.get("href") : null;
            if (href != null) {
                String idStr = href.substring(href.lastIndexOf('/') + 1);
                r.put("regionId", new BigDecimal(idStr));
            }
        }
        return regions;
    }
    private void updateRegionAssociation(String countryId, java.math.BigDecimal regionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/uri-list"));
        HttpEntity<String> req = new HttpEntity<>(baseUrl + "/regions/" + regionId, headers);
        restTemplate.exchange(
                baseUrl + "/countries/" + countryId + "/region",
                HttpMethod.PUT,
                req,
                Void.class);
    }

    

    private Map<String, Object> buildPayload(CountryDTO dto) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("countryId",   dto.getCountryId());
        payload.put("countryName", dto.getCountryName());
        payload.put("region",      "/api/v1/regions/" + dto.getRegionId());
        return payload;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

package com.example.HR_Management_Frontend.service;

import com.example.HR_Management_Frontend.dto.RegionDetailDto;
import com.example.HR_Management_Frontend.model.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegionService {

    private final RestTemplate restTemplate;
    private static final Pattern LAST_PATH_SEGMENT = Pattern.compile(".*/([^/?#]+)(?:[?#].*)?$");

    @Value("${backend.base-url:http://localhost:8080/api/v1}")
    private String baseUrl;

    public RegionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ===== Helper for _embedded =====
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Map<String, Object> response, String key) {
        if (response == null) return List.of();
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        if (embedded == null) return List.of();
        return (List<Map<String, Object>>) embedded.getOrDefault(key, List.of());
    }

    @SuppressWarnings("unchecked")
    private String extractHref(Map<String, Object> resource, String rel) {
        if (resource == null) return null;
        Map<String, Object> links = (Map<String, Object>) resource.get("_links");
        if (links == null) return null;
        Map<String, Object> link = (Map<String, Object>) links.get(rel);
        return link == null ? null : (String) link.get("href");
    }

    private Long extractLongId(Map<String, Object> resource, String idField) {
        Object id = resource.get(idField);
        if (id instanceof Number number) {
            return number.longValue();
        }

        String href = extractHref(resource, "self");
        if (href == null) return null;

        Matcher matcher = LAST_PATH_SEGMENT.matcher(href);
        if (!matcher.matches()) return null;

        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractStringId(Map<String, Object> resource, String idField) {
        Object id = resource.get(idField);
        if (id != null) return String.valueOf(id);

        String href = extractHref(resource, "self");
        if (href == null) return null;

        Matcher matcher = LAST_PATH_SEGMENT.matcher(href);
        return matcher.matches() ? matcher.group(1) : null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) return null;
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // ===== Page 2 =====
    public List<Region> getAllRegions() {

        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                baseUrl + "/regions",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> regionsList = extractList(resp.getBody(), "regions");

        return regionsList.stream().map(m -> {
            Region r = new Region();
            r.setRegionId(extractLongId(m, "regionId"));
            r.setRegionName((String) m.get("regionName"));
            return r;
        }).filter(r -> r.getRegionId() != null).toList();
    }

    // ===== Page 3 =====
    @SuppressWarnings("unchecked")
    public RegionDetailDto getRegionDetail(Long regionId) {

        RegionDetailDto dto = new RegionDetailDto();

        // REGION
        Map<String, Object> regionMap = restTemplate.getForObject(
                baseUrl + "/regions/" + regionId, Map.class);

        dto.setRegionId(regionId);
        dto.setRegionName((String) regionMap.get("regionName"));

        // COUNTRIES
        ResponseEntity<Map<String, Object>> countriesResp = restTemplate.exchange(
                baseUrl + "/regions/" + regionId + "/countries",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> countries = extractList(countriesResp.getBody(), "countries");

        List<RegionDetailDto.CountryDto> regionCountries = countries.stream()
                .map(c -> new RegionDetailDto.CountryDto(
                        extractStringId(c, "countryId"),
                        (String) c.get("countryName")))
                .toList();

        dto.setCountries(regionCountries);

        List<RegionDetailDto.LocationDto> regionLocs = new ArrayList<>();
        long employeeCount = 0;

        for (RegionDetailDto.CountryDto country : regionCountries) {
            if (country.getCountryId() == null) continue;

            ResponseEntity<Map<String, Object>> locsResp = restTemplate.exchange(
                    baseUrl + "/locations/search/findByCountry_CountryId?countryId=" + country.getCountryId(),
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});

            for (Map<String, Object> locationMap : extractList(locsResp.getBody(), "locations")) {
                Long locationId = extractLongId(locationMap, "id");
                if (locationId == null) continue;

                Map<String, Object> countMap = restTemplate.getForObject(
                        baseUrl + "/locations/" + locationId + "/employee-count",
                        Map.class);

                Long count = countMap == null ? null : asLong(countMap.get("employeeCount"));
                employeeCount += count == null ? 0 : count;

                if (locationMap.containsKey("city")) {
                    RegionDetailDto.LocationDto loc = new RegionDetailDto.LocationDto();
                    loc.setLocationId(locationId);
                    loc.setCity((String) locationMap.get("city"));
                    loc.setStreetAddress((String) locationMap.get("streetAddress"));
                    loc.setPostalCode((String) locationMap.get("postalCode"));
                    loc.setStateProvince((String) locationMap.get("stateProvince"));
                    loc.setCountryName(country.getCountryName());
                    regionLocs.add(loc);
                }
            }
        }

        dto.setLocations(regionLocs);
        dto.setActiveLocations(regionLocs.size());
        dto.setEmployees(List.of());
        dto.setActiveEmployees(Math.toIntExact(employeeCount));

        return dto;
    }
}

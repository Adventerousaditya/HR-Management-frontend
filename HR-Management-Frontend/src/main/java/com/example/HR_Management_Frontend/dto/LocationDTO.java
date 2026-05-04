package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDTO {

    private Long locationId;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String stateProvince;
    
    // This must be an object to match your JSON structure
    private CountryDTO country;

    @JsonProperty("_links")
    private Links links;

    // ── Getters & Setters ──

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }

    /** 
     * This method prevents the NullPointerException in Thymeleaf.
     * It checks if the country object exists before grabbing the ID.
     */
    public String getCountryId() { 
        return (country != null) ? country.getCountryId() : "—"; 
    }
    public CountryDTO getCountry() { return country; }
    public void setCountry(CountryDTO country) { this.country = country; }

    public Links getLinks() { return links; }
    public void setLinks(Links links) { this.links = links; }

    // ── Nested DTO for Country ──
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryDTO {
        private String countryId;
        private String countryName;

        public String getCountryId() { return countryId; }
        public void setCountryId(String countryId) { this.countryId = countryId; }
        public String getCountryName() { return countryName; }
        public void setCountryName(String countryName) { this.countryName = countryName; }
    }

    /** Parse numeric ID from HAL self link e.g. .../locations/1000 */
    public Long extractId() {
        if (links == null || links.getSelf() == null) return null;
        String href = links.getSelf().getHref();
        try {
            String[] parts = href.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) { return null; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;
        public HRef getSelf() { return self; }
        public void setSelf(HRef self) { this.self = self; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;
        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }
}
package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the nested "location" object inside a Department HAL response.
 *
 * Backend entity fields: id (location_id), streetAddress, postalCode, city,
 *                        stateProvince, country
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDTO {

    private java.math.BigDecimal id;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String stateProvince;

    public LocationDTO() {}

    public java.math.BigDecimal getId()             { return id; }
    public void setId(java.math.BigDecimal id)      { this.id = id; }

    public String getStreetAddress()                { return streetAddress; }
    public void setStreetAddress(String v)          { this.streetAddress = v; }

    public String getPostalCode()                   { return postalCode; }
    public void setPostalCode(String v)             { this.postalCode = v; }

    public String getCity()                         { return city; }
    public void setCity(String v)                   { this.city = v; }

    public String getStateProvince()                { return stateProvince; }
    public void setStateProvince(String v)          { this.stateProvince = v; }
}

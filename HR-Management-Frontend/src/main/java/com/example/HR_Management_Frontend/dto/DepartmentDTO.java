package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentDTO {

    private Long   departmentId;
    private String departmentName;
    private Long   managerId;

    // Populated from location association endpoint
    private String locationCity;
    private String locationState;
    private Long   locationId;

    @JsonProperty("_links")
    private Links links;

    public Long   getDepartmentId()   { return departmentId; }
    public void   setDepartmentId(Long v)   { this.departmentId = v; }

    public String getDepartmentName() { return departmentName; }
    public void   setDepartmentName(String v) { this.departmentName = v; }

    public Long   getManagerId()      { return managerId; }
    public void   setManagerId(Long v)      { this.managerId = v; }

    public String getLocationCity()   { return locationCity; }
    public void   setLocationCity(String v)  { this.locationCity = v; }

    public String getLocationState()  { return locationState; }
    public void   setLocationState(String v) { this.locationState = v; }

    public Long   getLocationId()     { return locationId; }
    public void   setLocationId(Long v)     { this.locationId = v; }

    /** Returns "City, State" or just "City" or "—" */
    public String getLocationDisplay() {
        if (locationCity == null) return "—";
        if (locationState == null) return locationCity;
        return locationCity + ", " + locationState;
    }

    public Links getLinks() { return links; }
    public void  setLinks(Links l) { this.links = l; }

    public Long extractId() {
        if (links == null || links.self == null) return null;
        try {
            String[] p = links.self.href.split("/");
            return Long.parseLong(p[p.length - 1]);
        } catch (Exception e) { return null; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private HRef self;
        public HRef getSelf() { return self; }
        public void setSelf(HRef s) { this.self = s; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HRef {
        private String href;
        public String getHref() { return href; }
        public void setHref(String h) { this.href = h; }
    }
}
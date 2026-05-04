package com.example.HR_Management_Frontend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CountryDTO {
	    @NotBlank(message = "Country ID is required")
	    @Size(max = 4, message = "Country ID must not exceed 4 characters")
	    @Pattern(regexp = "^[a-zA-Z]+$", message = "Country ID must contain only letters")
	    private String countryId;
	 
	    @NotBlank(message = "Country name must not be blank")
	    private String countryName;
	 
	    @NotNull(message = "Region is required")
	    private BigDecimal regionId;
	 
	    public String getCountryId() {
	        return countryId;
	    }
	 
	    public void setCountryId(String countryId) {
	        this.countryId = countryId;
	    }
	 
	    public String getCountryName() {
	        return countryName;
	    }
	 
	    public void setCountryName(String countryName) {
	        this.countryName = countryName;
	    }
	 
	    public BigDecimal getRegionId() {
	        return regionId;
	    }
	 
	    public void setRegionId(BigDecimal regionId) {
	        this.regionId = regionId;
	    }
}

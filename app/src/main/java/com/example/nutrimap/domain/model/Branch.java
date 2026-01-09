package com.example.nutrimap.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Branch {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bn_name")
    private String bnName;

    @JsonProperty("Area")
    private String area;

    @JsonProperty("bn_Area")
    private String bnArea;

    @JsonProperty("Upazilla")
    private String upazila;

    @JsonProperty("bn_Upazilla")
    private String bnUpazila;

    @JsonProperty("District")
    private String district;

    @JsonProperty("bn_District")
    private String bnDistrict;

    @JsonProperty("Division")
    private String division;

    @JsonProperty("bn_Division")
    private String bnDivision;

    @JsonProperty("url")
    private String url;

    public Branch() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBnName() { return bnName; }
    public void setBnName(String bnName) { this.bnName = bnName; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getBnArea() { return bnArea; }
    public void setBnArea(String bnArea) { this.bnArea = bnArea; }

    public String getUpazila() { return upazila; }
    public void setUpazila(String upazila) { this.upazila = upazila; }

    public String getBnUpazila() { return bnUpazila; }
    public void setBnUpazila(String bnUpazila) { this.bnUpazila = bnUpazila; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getBnDistrict() { return bnDistrict; }
    public void setBnDistrict(String bnDistrict) { this.bnDistrict = bnDistrict; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getBnDivision() { return bnDivision; }
    public void setBnDivision(String bnDivision) { this.bnDivision = bnDivision; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFullLocation() {
        return upazila + ", " + district + ", " + division;
    }
}

package com.example.nutrimap.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Upazila {
    @JsonProperty("id")
    private String id;

    @JsonProperty("district_id")
    private String districtId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bn_name")
    private String bnName;

    @JsonProperty("url")
    private String url;

    public Upazila() {}

    public Upazila(String id, String districtId, String name, String bnName, String url) {
        this.id = id;
        this.districtId = districtId;
        this.name = name;
        this.bnName = bnName;
        this.url = url;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBnName() { return bnName; }
    public void setBnName(String bnName) { this.bnName = bnName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    @Override
    public String toString() {
        return name;
    }
}

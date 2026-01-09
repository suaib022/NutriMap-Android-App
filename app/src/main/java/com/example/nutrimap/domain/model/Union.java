package com.example.nutrimap.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Union {
    @JsonProperty("id")
    private String id;

    @JsonProperty("upazilla_id")
    private String upazilaId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("bn_name")
    private String bnName;

    @JsonProperty("url")
    private String url;

    public Union() {}

    public Union(String id, String upazilaId, String name, String bnName, String url) {
        this.id = id;
        this.upazilaId = upazilaId;
        this.name = name;
        this.bnName = bnName;
        this.url = url;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUpazilaId() { return upazilaId; }
    public void setUpazilaId(String upazilaId) { this.upazilaId = upazilaId; }

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

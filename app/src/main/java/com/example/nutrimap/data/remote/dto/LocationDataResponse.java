package com.example.nutrimap.data.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * DTO for parsing the nested JSON format from PHPMyAdmin exports.
 * The JSON structure is: [header, database, {type: "table", data: [...]}]
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDataResponse extends java.util.ArrayList<LocationDataResponse.JsonElement> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonElement {
        @JsonProperty("type")
        public String type;

        @JsonProperty("name")
        public String name;

        @JsonProperty("data")
        public List<Map<String, String>> data;
    }

    /**
     * Extract the actual data array from the nested JSON structure.
     * @return List of data maps, or empty list if not found
     */
    public List<Map<String, String>> extractData() {
        for (JsonElement element : this) {
            if ("table".equals(element.type) && element.data != null) {
                return element.data;
            }
        }
        return new java.util.ArrayList<>();
    }
}

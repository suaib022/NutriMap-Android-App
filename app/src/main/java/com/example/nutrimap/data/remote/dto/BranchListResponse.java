package com.example.nutrimap.data.remote.dto;

import com.example.nutrimap.domain.model.Branch;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * DTO for parsing the branches JSON which is a simple array of Branch objects.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchListResponse extends ArrayList<Branch> {
    // No additional members needed - this just extends ArrayList<Branch>
    // Jackson will directly deserialize the JSON array into this list
}

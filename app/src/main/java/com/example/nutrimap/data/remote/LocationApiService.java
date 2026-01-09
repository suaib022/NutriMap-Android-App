package com.example.nutrimap.data.remote;

import com.example.nutrimap.data.remote.dto.BranchListResponse;
import com.example.nutrimap.data.remote.dto.LocationDataResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit service interface for fetching location and branch data from GitHub.
 */
public interface LocationApiService {

    @GET("divisions.json")
    Call<LocationDataResponse> getDivisions();

    @GET("districts.json")
    Call<LocationDataResponse> getDistricts();

    @GET("upazilas.json")
    Call<LocationDataResponse> getUpazilas();

    @GET("unions.json")
    Call<LocationDataResponse> getUnions();

    @GET("branches.json")
    Call<BranchListResponse> getBranches();
}

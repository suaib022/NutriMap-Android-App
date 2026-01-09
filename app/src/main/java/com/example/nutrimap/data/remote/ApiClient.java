package com.example.nutrimap.data.remote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Singleton Retrofit client for accessing GitHub raw JSON endpoints.
 */
public class ApiClient {

    private static final String BASE_URL = "https://raw.githubusercontent.com/suaib022/NutriMap-Dekstop-Version/main/src/main/resources/data/";

    private static Retrofit retrofit = null;
    private static LocationApiService locationApiService = null;

    private ApiClient() {
        // Private constructor to prevent instantiation
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build();
        }
        return retrofit;
    }

    public static LocationApiService getLocationApiService() {
        if (locationApiService == null) {
            locationApiService = getRetrofit().create(LocationApiService.class);
        }
        return locationApiService;
    }
}

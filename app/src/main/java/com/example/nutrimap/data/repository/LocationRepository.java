package com.example.nutrimap.data.repository;

import android.util.Log;

import com.example.nutrimap.data.remote.ApiClient;
import com.example.nutrimap.data.remote.LocationApiService;
import com.example.nutrimap.data.remote.dto.LocationDataResponse;
import com.example.nutrimap.domain.model.District;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Union;
import com.example.nutrimap.domain.model.Upazila;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for fetching and caching location data (divisions, districts, upazilas, unions).
 */
public class LocationRepository {

    private static final String TAG = "LocationRepository";

    private static LocationRepository instance;

    private final LocationApiService apiService;

    // Cached data
    private List<Division> cachedDivisions;
    private List<District> cachedDistricts;
    private List<Upazila> cachedUpazilas;
    private List<Union> cachedUnions;

    private LocationRepository() {
        apiService = ApiClient.getLocationApiService();
    }

    public static synchronized LocationRepository getInstance() {
        if (instance == null) {
            instance = new LocationRepository();
        }
        return instance;
    }

    // ==================== DIVISIONS ====================

    public interface DivisionCallback {
        void onSuccess(List<Division> divisions);
        void onError(String message);
    }

    public void getDivisions(DivisionCallback callback) {
        if (cachedDivisions != null) {
            callback.onSuccess(cachedDivisions);
            return;
        }

        apiService.getDivisions().enqueue(new Callback<LocationDataResponse>() {
            @Override
            public void onResponse(Call<LocationDataResponse> call, Response<LocationDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> data = response.body().extractData();
                    cachedDivisions = new ArrayList<>();
                    for (Map<String, String> item : data) {
                        Division div = new Division(
                                item.get("id"),
                                item.get("name"),
                                item.get("bn_name"),
                                item.get("url")
                        );
                        cachedDivisions.add(div);
                    }
                    callback.onSuccess(cachedDivisions);
                } else {
                    callback.onError("Failed to load divisions");
                }
            }

            @Override
            public void onFailure(Call<LocationDataResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching divisions", t);
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== DISTRICTS ====================

    public interface DistrictCallback {
        void onSuccess(List<District> districts);
        void onError(String message);
    }

    public void getDistricts(DistrictCallback callback) {
        if (cachedDistricts != null) {
            callback.onSuccess(cachedDistricts);
            return;
        }

        apiService.getDistricts().enqueue(new Callback<LocationDataResponse>() {
            @Override
            public void onResponse(Call<LocationDataResponse> call, Response<LocationDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> data = response.body().extractData();
                    cachedDistricts = new ArrayList<>();
                    for (Map<String, String> item : data) {
                        District dist = new District(
                                item.get("id"),
                                item.get("division_id"),
                                item.get("name"),
                                item.get("bn_name"),
                                item.get("url")
                        );
                        cachedDistricts.add(dist);
                    }
                    callback.onSuccess(cachedDistricts);
                } else {
                    callback.onError("Failed to load districts");
                }
            }

            @Override
            public void onFailure(Call<LocationDataResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching districts", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getDistrictsByDivision(String divisionId, DistrictCallback callback) {
        getDistricts(new DistrictCallback() {
            @Override
            public void onSuccess(List<District> districts) {
                List<District> filtered = new ArrayList<>();
                for (District d : districts) {
                    if (divisionId.equals(d.getDivisionId())) {
                        filtered.add(d);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== UPAZILAS ====================

    public interface UpazilaCallback {
        void onSuccess(List<Upazila> upazilas);
        void onError(String message);
    }

    public void getUpazilas(UpazilaCallback callback) {
        if (cachedUpazilas != null) {
            callback.onSuccess(cachedUpazilas);
            return;
        }

        apiService.getUpazilas().enqueue(new Callback<LocationDataResponse>() {
            @Override
            public void onResponse(Call<LocationDataResponse> call, Response<LocationDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> data = response.body().extractData();
                    cachedUpazilas = new ArrayList<>();
                    for (Map<String, String> item : data) {
                        Upazila up = new Upazila(
                                item.get("id"),
                                item.get("district_id"),
                                item.get("name"),
                                item.get("bn_name"),
                                item.get("url")
                        );
                        cachedUpazilas.add(up);
                    }
                    callback.onSuccess(cachedUpazilas);
                } else {
                    callback.onError("Failed to load upazilas");
                }
            }

            @Override
            public void onFailure(Call<LocationDataResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching upazilas", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUpazilasByDistrict(String districtId, UpazilaCallback callback) {
        getUpazilas(new UpazilaCallback() {
            @Override
            public void onSuccess(List<Upazila> upazilas) {
                List<Upazila> filtered = new ArrayList<>();
                for (Upazila u : upazilas) {
                    if (districtId.equals(u.getDistrictId())) {
                        filtered.add(u);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== UNIONS ====================

    public interface UnionCallback {
        void onSuccess(List<Union> unions);
        void onError(String message);
    }

    public void getUnions(UnionCallback callback) {
        if (cachedUnions != null) {
            callback.onSuccess(cachedUnions);
            return;
        }

        apiService.getUnions().enqueue(new Callback<LocationDataResponse>() {
            @Override
            public void onResponse(Call<LocationDataResponse> call, Response<LocationDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> data = response.body().extractData();
                    cachedUnions = new ArrayList<>();
                    for (Map<String, String> item : data) {
                        Union un = new Union(
                                item.get("id"),
                                item.get("upazilla_id"),
                                item.get("name"),
                                item.get("bn_name"),
                                item.get("url")
                        );
                        cachedUnions.add(un);
                    }
                    callback.onSuccess(cachedUnions);
                } else {
                    callback.onError("Failed to load unions");
                }
            }

            @Override
            public void onFailure(Call<LocationDataResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching unions", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUnionsByUpazila(String upazilaId, UnionCallback callback) {
        getUnions(new UnionCallback() {
            @Override
            public void onSuccess(List<Union> unions) {
                List<Union> filtered = new ArrayList<>();
                for (Union u : unions) {
                    if (upazilaId.equals(u.getUpazilaId())) {
                        filtered.add(u);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== UTILITY ====================

    public void clearCache() {
        cachedDivisions = null;
        cachedDistricts = null;
        cachedUpazilas = null;
        cachedUnions = null;
    }
}

package com.example.nutrimap.data.repository;

import android.util.Log;

import com.example.nutrimap.data.remote.ApiClient;
import com.example.nutrimap.data.remote.LocationApiService;
import com.example.nutrimap.data.remote.dto.BranchListResponse;
import com.example.nutrimap.domain.model.Branch;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for fetching and filtering branch data.
 */
public class BranchRepository {

    private static final String TAG = "BranchRepository";

    private static BranchRepository instance;

    private final LocationApiService apiService;
    private List<Branch> cachedBranches;

    private BranchRepository() {
        apiService = ApiClient.getLocationApiService();
    }

    public static synchronized BranchRepository getInstance() {
        if (instance == null) {
            instance = new BranchRepository();
        }
        return instance;
    }

    public interface BranchCallback {
        void onSuccess(List<Branch> branches);
        void onError(String message);
    }

    public void getBranches(BranchCallback callback) {
        if (cachedBranches != null) {
            callback.onSuccess(cachedBranches);
            return;
        }

        apiService.getBranches().enqueue(new Callback<BranchListResponse>() {
            @Override
            public void onResponse(Call<BranchListResponse> call, Response<BranchListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedBranches = new ArrayList<>(response.body());
                    callback.onSuccess(cachedBranches);
                } else {
                    callback.onError("Failed to load branches");
                }
            }

            @Override
            public void onFailure(Call<BranchListResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching branches", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getBranchesByLocation(String division, String district, String upazila, BranchCallback callback) {
        getBranches(new BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                List<Branch> filtered = new ArrayList<>();
                for (Branch b : branches) {
                    boolean matches = true;

                    if (division != null && !division.isEmpty()) {
                        matches = division.equalsIgnoreCase(b.getDivision());
                    }
                    if (matches && district != null && !district.isEmpty()) {
                        matches = district.equalsIgnoreCase(b.getDistrict());
                    }
                    if (matches && upazila != null && !upazila.isEmpty()) {
                        matches = upazila.equalsIgnoreCase(b.getUpazila());
                    }

                    if (matches) {
                        filtered.add(b);
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

    public void searchBranches(String query, BranchCallback callback) {
        getBranches(new BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                if (query == null || query.isEmpty()) {
                    callback.onSuccess(branches);
                    return;
                }

                String lowerQuery = query.toLowerCase();
                List<Branch> filtered = new ArrayList<>();
                for (Branch b : branches) {
                    if (b.getName().toLowerCase().contains(lowerQuery) ||
                            b.getDistrict().toLowerCase().contains(lowerQuery) ||
                            b.getDivision().toLowerCase().contains(lowerQuery) ||
                            b.getUpazila().toLowerCase().contains(lowerQuery)) {
                        filtered.add(b);
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

    public void clearCache() {
        cachedBranches = null;
    }
}

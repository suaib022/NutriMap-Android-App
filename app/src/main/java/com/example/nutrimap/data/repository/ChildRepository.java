package com.example.nutrimap.data.repository;

import com.example.nutrimap.data.firebase.FirebaseDataService;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for child data access using Firebase Firestore.
 */
public class ChildRepository {

    private static ChildRepository instance;

    private ChildRepository() {}

    public static synchronized ChildRepository getInstance() {
        if (instance == null) {
            instance = new ChildRepository();
        }
        return instance;
    }

    // Callback interfaces
    public interface ChildrenCallback {
        void onSuccess(List<Child> children);
        void onError(String message);
    }

    public interface ChildCallback {
        void onSuccess(Child child);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    // ==================== FIREBASE CRUD ====================

    public void getAllChildren(ChildrenCallback callback) {
        FirebaseDataService.getInstance().getAllChildren(new FirebaseDataService.DataCallback<List<Child>>() {
            @Override
            public void onSuccess(List<Child> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getChildById(String documentId, ChildCallback callback) {
        FirebaseDataService.getInstance().getChildById(documentId, new FirebaseDataService.DataCallback<Child>() {
            @Override
            public void onSuccess(Child data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void addChild(Child child, ChildCallback callback) {
        FirebaseDataService.getInstance().addChild(child, new FirebaseDataService.DataCallback<Child>() {
            @Override
            public void onSuccess(Child data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateChild(Child child, OperationCallback callback) {
        FirebaseDataService.getInstance().updateChild(child, new FirebaseDataService.OperationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void deleteChild(String documentId, OperationCallback callback) {
        FirebaseDataService.getInstance().deleteChild(documentId, new FirebaseDataService.OperationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== SEARCH AND FILTER ====================

    public void searchChildren(String query, ChildrenCallback callback) {
        getAllChildren(new ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                if (query == null || query.isEmpty()) {
                    callback.onSuccess(children);
                    return;
                }
                String lowerQuery = query.toLowerCase();
                List<Child> result = new ArrayList<>();
                for (Child c : children) {
                    if (c.getName().toLowerCase().contains(lowerQuery) ||
                            c.getFatherName().toLowerCase().contains(lowerQuery) ||
                            c.getMotherName().toLowerCase().contains(lowerQuery)) {
                        result.add(c);
                    }
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void filterChildrenByLocation(String divisionId, String districtId,
                                         String upazilaId, String unionId, ChildrenCallback callback) {
        getAllChildren(new ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                List<Child> result = new ArrayList<>();
                for (Child c : children) {
                    boolean matches = true;
                    if (divisionId != null && !divisionId.isEmpty()) {
                        matches = divisionId.equals(c.getDivisionId());
                    }
                    if (matches && districtId != null && !districtId.isEmpty()) {
                        matches = districtId.equals(c.getDistrictId());
                    }
                    if (matches && upazilaId != null && !upazilaId.isEmpty()) {
                        matches = upazilaId.equals(c.getUpazilaId());
                    }
                    if (matches && unionId != null && !unionId.isEmpty()) {
                        matches = unionId.equals(c.getUnionId());
                    }
                    if (matches) {
                        result.add(c);
                    }
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== DASHBOARD STATS ====================

    public void getDashboardStats(DashboardStatsCallback callback) {
        getAllChildren(new ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                VisitRepository.getInstance().getAllVisits(new VisitRepository.VisitsCallback() {
                    @Override
                    public void onSuccess(List<Visit> visits) {
                        int total = children.size();
                        int highRisk = 0;
                        int mediumRisk = 0;
                        int lowRisk = 0;

                        for (Child c : children) {
                            String risk = getRiskLevelFromVisits(c.getDocumentId(), visits);
                            switch (risk) {
                                case "High": highRisk++; break;
                                case "Medium": mediumRisk++; break;
                                case "Low": lowRisk++; break;
                            }
                        }
                        callback.onSuccess(new DashboardStats(total, highRisk, mediumRisk, lowRisk));
                    }

                    @Override
                    public void onError(String message) {
                        // Return stats with what we have
                        callback.onSuccess(new DashboardStats(children.size(), 0, 0, 0));
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private String getRiskLevelFromVisits(String childDocumentId, List<Visit> allVisits) {
        Visit latest = null;
        for (Visit v : allVisits) {
            if (childDocumentId != null && childDocumentId.equals(v.getChildDocumentId())) {
                if (latest == null || v.getVisitDate().compareTo(latest.getVisitDate()) > 0) {
                    latest = v;
                }
            }
        }
        if (latest == null) return "N/A";
        return NutritionRiskCalculator.calculateRiskFromMuac(latest.getMuacMm());
    }

    public interface DashboardStatsCallback {
        void onSuccess(DashboardStats stats);
        void onError(String message);
    }

    public static class DashboardStats {
        public final int totalChildren;
        public final int highRisk;
        public final int mediumRisk;
        public final int lowRisk;

        public DashboardStats(int totalChildren, int highRisk, int mediumRisk, int lowRisk) {
            this.totalChildren = totalChildren;
            this.highRisk = highRisk;
            this.mediumRisk = mediumRisk;
            this.lowRisk = lowRisk;
        }
    }
}

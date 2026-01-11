package com.example.nutrimap.data.repository;

import com.example.nutrimap.data.firebase.FirebaseDataService;
import com.example.nutrimap.domain.model.Visit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for visit data access using Firebase Firestore.
 */
public class VisitRepository {

    private static VisitRepository instance;

    private VisitRepository() {}

    public static synchronized VisitRepository getInstance() {
        if (instance == null) {
            instance = new VisitRepository();
        }
        return instance;
    }

    // Callback interfaces
    public interface VisitsCallback {
        void onSuccess(List<Visit> visits);
        void onError(String message);
    }

    public interface VisitCallback {
        void onSuccess(Visit visit);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onError(String message);
    }

    // ==================== FIREBASE CRUD ====================

    public void getAllVisits(VisitsCallback callback) {
        FirebaseDataService.getInstance().getAllVisits(new FirebaseDataService.DataCallback<List<Visit>>() {
            @Override
            public void onSuccess(List<Visit> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getVisitsForChild(String childDocumentId, VisitsCallback callback) {
        FirebaseDataService.getInstance().getVisitsForChild(childDocumentId, new FirebaseDataService.DataCallback<List<Visit>>() {
            @Override
            public void onSuccess(List<Visit> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void addVisit(Visit visit, VisitCallback callback) {
        FirebaseDataService.getInstance().addVisit(visit, new FirebaseDataService.DataCallback<Visit>() {
            @Override
            public void onSuccess(Visit data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateVisit(Visit visit, OperationCallback callback) {
        FirebaseDataService.getInstance().updateVisit(visit, new FirebaseDataService.OperationCallback() {
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

    public void deleteVisit(String documentId, OperationCallback callback) {
        FirebaseDataService.getInstance().deleteVisit(documentId, new FirebaseDataService.OperationCallback() {
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

    public void searchVisits(String query, VisitsCallback callback) {
        getAllVisits(new VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                if (query == null || query.isEmpty()) {
                    callback.onSuccess(visits);
                    return;
                }
                String lowerQuery = query.toLowerCase();
                List<Visit> result = new ArrayList<>();
                for (Visit v : visits) {
                    String notes = v.getNotes() != null ? v.getNotes().toLowerCase() : "";
                    if (v.getVisitDate().contains(query) || notes.contains(lowerQuery)) {
                        result.add(v);
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

    public void getLatestVisitForChild(String childDocumentId, VisitCallback callback) {
        getVisitsForChild(childDocumentId, new VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                Visit latest = null;
                for (Visit v : visits) {
                    if (latest == null || v.getVisitDate().compareTo(latest.getVisitDate()) > 0) {
                        latest = v;
                    }
                }
                if (latest != null) {
                    callback.onSuccess(latest);
                } else {
                    callback.onError("No visits found");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== STATISTICS ====================

    public void getTotalVisitCount(CountCallback callback) {
        getAllVisits(new VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                callback.onSuccess(visits.size());
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getVisitsPerMonth(VisitsPerMonthCallback callback) {
        getAllVisits(new VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                Map<String, Integer> monthCounts = new HashMap<>();
                for (Visit v : visits) {
                    String date = v.getVisitDate();
                    if (date != null && date.length() >= 7) {
                        String month = date.substring(0, 7); // yyyy-MM
                        monthCounts.put(month, monthCounts.getOrDefault(month, 0) + 1);
                    }
                }
                callback.onSuccess(monthCounts);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public interface VisitsPerMonthCallback {
        void onSuccess(Map<String, Integer> monthCounts);
        void onError(String message);
    }
}

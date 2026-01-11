package com.example.nutrimap.data.firebase;

import android.util.Log;

import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.domain.model.Visit;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Central Firebase Firestore service for all data operations.
 */
public class FirebaseDataService {

    private static final String TAG = "FirebaseDataService";
    
    public static final String COLLECTION_CHILDREN = "children";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_VISITS = "visits";

    private static FirebaseDataService instance;
    private final FirebaseFirestore db;

    private FirebaseDataService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseDataService getInstance() {
        if (instance == null) {
            instance = new FirebaseDataService();
        }
        return instance;
    }

    // ==================== CALLBACKS ====================

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    // ==================== CHILDREN ====================

    public void getAllChildren(DataCallback<List<Child>> callback) {
        db.collection(COLLECTION_CHILDREN)
                .orderBy("name")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Child> children = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Child child = doc.toObject(Child.class);
                        child.setDocumentId(doc.getId());
                        children.add(child);
                    }
                    callback.onSuccess(children);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting children", e);
                    callback.onError(e.getMessage());
                });
    }

    public void getChildById(String documentId, DataCallback<Child> callback) {
        db.collection(COLLECTION_CHILDREN).document(documentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Child child = doc.toObject(Child.class);
                        child.setDocumentId(doc.getId());
                        callback.onSuccess(child);
                    } else {
                        callback.onError("Child not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addChild(Child child, DataCallback<Child> callback) {
        db.collection(COLLECTION_CHILDREN)
                .add(child)
                .addOnSuccessListener(docRef -> {
                    child.setDocumentId(docRef.getId());
                    callback.onSuccess(child);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding child", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateChild(Child child, OperationCallback callback) {
        if (child.getDocumentId() == null || child.getDocumentId().isEmpty()) {
            callback.onError("Document ID is required for update");
            return;
        }
        db.collection(COLLECTION_CHILDREN).document(child.getDocumentId())
                .set(child)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating child", e);
                    callback.onError(e.getMessage());
                });
    }

    public void deleteChild(String documentId, OperationCallback callback) {
        db.collection(COLLECTION_CHILDREN).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Also delete associated visits
                    deleteVisitsForChild(documentId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== USERS ====================

    public void getAllUsers(DataCallback<List<User>> callback) {
        db.collection(COLLECTION_USERS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        user.setDocumentId(doc.getId());
                        users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting users", e);
                    callback.onError(e.getMessage());
                });
    }

    public void getUserByEmail(String email, DataCallback<User> callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        User user = doc.toObject(User.class);
                        user.setDocumentId(doc.getId());
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addUser(User user, DataCallback<User> callback) {
        db.collection(COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(docRef -> {
                    user.setDocumentId(docRef.getId());
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateUser(User user, OperationCallback callback) {
        if (user.getDocumentId() == null || user.getDocumentId().isEmpty()) {
            callback.onError("Document ID is required for update");
            return;
        }
        db.collection(COLLECTION_USERS).document(user.getDocumentId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteUser(String documentId, OperationCallback callback) {
        db.collection(COLLECTION_USERS).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== VISITS ====================

    public void getAllVisits(DataCallback<List<Visit>> callback) {
        db.collection(COLLECTION_VISITS)
                .orderBy("visitDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Visit> visits = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Visit visit = doc.toObject(Visit.class);
                        visit.setDocumentId(doc.getId());
                        visits.add(visit);
                    }
                    callback.onSuccess(visits);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting visits", e);
                    callback.onError(e.getMessage());
                });
    }

    public void getVisitsForChild(String childDocumentId, DataCallback<List<Visit>> callback) {
        db.collection(COLLECTION_VISITS)
                .whereEqualTo("childDocumentId", childDocumentId)
                .orderBy("visitDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Visit> visits = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Visit visit = doc.toObject(Visit.class);
                        visit.setDocumentId(doc.getId());
                        visits.add(visit);
                    }
                    callback.onSuccess(visits);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addVisit(Visit visit, DataCallback<Visit> callback) {
        db.collection(COLLECTION_VISITS)
                .add(visit)
                .addOnSuccessListener(docRef -> {
                    visit.setDocumentId(docRef.getId());
                    callback.onSuccess(visit);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateVisit(Visit visit, OperationCallback callback) {
        if (visit.getDocumentId() == null || visit.getDocumentId().isEmpty()) {
            callback.onError("Document ID is required for update");
            return;
        }
        db.collection(COLLECTION_VISITS).document(visit.getDocumentId())
                .set(visit)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteVisit(String documentId, OperationCallback callback) {
        db.collection(COLLECTION_VISITS).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void deleteVisitsForChild(String childDocumentId) {
        db.collection(COLLECTION_VISITS)
                .whereEqualTo("childDocumentId", childDocumentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    // ==================== SAMPLE DATA SEEDING ====================

    public void seedSampleData(OperationCallback callback) {
        // Check if data already exists
        db.collection(COLLECTION_CHILDREN).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        seedChildren();
                        seedUsers();
                        seedVisits();
                        callback.onSuccess();
                    } else {
                        callback.onSuccess(); // Data already exists
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void seedChildren() {
        CollectionReference ref = db.collection(COLLECTION_CHILDREN);
        
        ref.add(new Child(0, "Arif Rahman", "Kamal Rahman", "Fatima Rahman", "01712345678",
                "Male", "2022-03-15", "1", "1", "1", "1", "1"));
        ref.add(new Child(0, "Fatima Akter", "Jobbar Akter", "Hasina Akter", "01812345679",
                "Female", "2021-08-20", "1", "1", "1", "2", "1"));
        ref.add(new Child(0, "Rifat Hasan", "Belal Hasan", "Sumaya Hasan", "01912345680",
                "Male", "2023-01-10", "1", "1", "2", "16", "2"));
        ref.add(new Child(0, "Nadia Islam", "Raju Islam", "Roksana Islam", "01612345681",
                "Female", "2022-06-05", "1", "1", "3", "31", "3"));
        ref.add(new Child(0, "Imran Khan", "Aziz Khan", "Rokeya Khan", "01512345682",
                "Male", "2021-11-25", "6", "47", "339", "2900", "100"));
    }

    private void seedUsers() {
        CollectionReference ref = db.collection(COLLECTION_USERS);
        
        ref.add(new User(0, "Admin User", "admin@nutrimap.com", "admin123", "ADMIN", "", null));
        ref.add(new User(0, "Rafiq Hasan", "rafiq.hasan@example.com", "pass@2025", "USER", "", "1"));
        ref.add(new User(0, "Ayesha Rahman", "ayesha.rahman@example.com", "ayesha123", "USER", "", "1"));
        ref.add(new User(0, "Sakib Hossain", "sakib.hossain@example.com", "sakib@bd", "USER", "", "2"));
    }

    private void seedVisits() {
        // Visits will be added after children documents are created
        // For now, leave empty - visits can be added through the app
    }
}

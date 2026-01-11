package com.example.nutrimap.data.repository;

import com.example.nutrimap.data.firebase.FirebaseDataService;
import com.example.nutrimap.domain.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for user data access using Firebase Firestore.
 */
public class UserRepository {

    private static UserRepository instance;

    private UserRepository() {}

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // Callback interfaces
    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String message);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    // ==================== FIREBASE CRUD ====================

    public void getAllUsers(UsersCallback callback) {
        FirebaseDataService.getInstance().getAllUsers(new FirebaseDataService.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getUserByEmail(String email, UserCallback callback) {
        FirebaseDataService.getInstance().getUserByEmail(email, new FirebaseDataService.DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void addUser(User user, UserCallback callback) {
        FirebaseDataService.getInstance().addUser(user, new FirebaseDataService.DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateUser(User user, OperationCallback callback) {
        FirebaseDataService.getInstance().updateUser(user, new FirebaseDataService.OperationCallback() {
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

    public void deleteUser(String documentId, OperationCallback callback) {
        FirebaseDataService.getInstance().deleteUser(documentId, new FirebaseDataService.OperationCallback() {
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

    public void searchUsers(String query, UsersCallback callback) {
        getAllUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (query == null || query.isEmpty()) {
                    callback.onSuccess(users);
                    return;
                }
                String lowerQuery = query.toLowerCase();
                List<User> result = new ArrayList<>();
                for (User u : users) {
                    if (u.getName().toLowerCase().contains(lowerQuery) ||
                            u.getEmail().toLowerCase().contains(lowerQuery)) {
                        result.add(u);
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

    public void filterUsersByBranch(String branchId, UsersCallback callback) {
        getAllUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (branchId == null || branchId.isEmpty()) {
                    callback.onSuccess(users);
                    return;
                }
                List<User> result = new ArrayList<>();
                for (User u : users) {
                    if (branchId.equals(u.getBranchId())) {
                        result.add(u);
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
}

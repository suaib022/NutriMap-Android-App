package com.example.nutrimap.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nutrimap.domain.model.User;

/**
 * Manages user session using SharedPreferences.
 * Stores logged-in user info locally for role-based access.
 */
public class SessionManager {
    
    private static final String PREF_NAME = "NutriMapSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_DOC_ID = "userDocId";
    private static final String KEY_USER_IMAGE = "userImage";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Save user session after successful login
     */
    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_DOC_ID, user.getDocumentId());
        editor.putString(KEY_USER_IMAGE, user.getImagePath());
        editor.apply();
    }

    /**
     * Clear session on logout
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    public String getUserDocId() {
        return prefs.getString(KEY_USER_DOC_ID, "");
    }

    public String getUserImage() {
        return prefs.getString(KEY_USER_IMAGE, "");
    }

    // Role check helpers
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getUserRole());
    }

    public boolean isSupervisor() {
        return "SUPERVISOR".equalsIgnoreCase(getUserRole());
    }

    public boolean isFieldWorker() {
        return "FIELD_WORKER".equalsIgnoreCase(getUserRole());
    }
}

package com.example.nutrimap.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.nutrimap.R;
import com.example.nutrimap.data.session.SessionManager;
import com.example.nutrimap.databinding.ActivityMainBinding;
import com.example.nutrimap.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

/**
 * Main activity hosting the navigation drawer and fragment container.
 * Implements role-based navigation visibility using SessionManager.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPERVISOR = "SUPERVISOR";
    public static final String ROLE_FIELD_WORKER = "FIELD_WORKER";

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActionBarDrawerToggle toggle;
    private SessionManager sessionManager;
    private Set<Integer> topLevelDestinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupToolbar();
        setupNavigation();
        updateNavHeader();
        applyRoleBasedNavigation();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Define top-level destinations (these show hamburger menu, not back button)
        topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.homeFragment);
        topLevelDestinations.add(R.id.childrenFragment);
        topLevelDestinations.add(R.id.visitsFragment);
        topLevelDestinations.add(R.id.usersFragment);
        topLevelDestinations.add(R.id.branchesFragment);
        topLevelDestinations.add(R.id.profileFragment);

        // Configure top-level destinations (no back button)
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // Setup ActionBar with Navigation
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Handle navigation item selection
        binding.navigationView.setNavigationItemSelectedListener(this);

        // Setup drawer toggle
        toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.nav_home, R.string.nav_home
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Listen to destination changes to enable/disable drawer toggle
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isTopLevel = topLevelDestinations.contains(destination.getId());
            
            if (isTopLevel) {
                // Top-level destinations: enable drawer, show hamburger menu
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toggle.setDrawerIndicatorEnabled(true);
            } else {
                // Child destinations: disable drawer, show back button
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toggle.setDrawerIndicatorEnabled(false);
                
                // Setup click to navigate back
                toggle.setToolbarNavigationClickListener(v -> {
                    navController.navigateUp();
                });
            }
        });
    }

    /**
     * Apply role-based visibility to navigation menu items.
     * - Admin: Full access
     * - Supervisor: All except Users
     * - Field Worker: Only Home, Children, Visits, Profile
     */
    private void applyRoleBasedNavigation() {
        Menu navMenu = binding.navigationView.getMenu();
        String role = sessionManager.getUserRole();
        
        if (ROLE_FIELD_WORKER.equalsIgnoreCase(role)) {
            // Field worker: hide Users and Branches
            navMenu.findItem(R.id.nav_users).setVisible(false);
            navMenu.findItem(R.id.nav_branches).setVisible(false);
        } else if (ROLE_SUPERVISOR.equalsIgnoreCase(role)) {
            // Supervisor: hide Users only
            navMenu.findItem(R.id.nav_users).setVisible(false);
        }
        // Admin: show all (default)
    }

    private void updateNavHeader() {
        View headerView = binding.navigationView.getHeaderView(0);
        TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
        TextView textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

        // Set values from session
        textViewUserName.setText(sessionManager.getUserName());
        textViewUserEmail.setText(sessionManager.getUserEmail());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_logout) {
            logout();
        } else if (itemId == R.id.nav_home) {
            navController.navigate(R.id.homeFragment);
        } else if (itemId == R.id.nav_children) {
            navController.navigate(R.id.childrenFragment);
        } else if (itemId == R.id.nav_visits) {
            navController.navigate(R.id.visitsFragment);
        } else if (itemId == R.id.nav_users) {
            navController.navigate(R.id.usersFragment);
        } else if (itemId == R.id.nav_branches) {
            navController.navigate(R.id.branchesFragment);
        } else if (itemId == R.id.nav_profile) {
            navController.navigate(R.id.profileFragment);
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        sessionManager.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getCurrentUserEmail() {
        return sessionManager.getUserEmail();
    }

    public String getCurrentUserRole() {
        return sessionManager.getUserRole();
    }

    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }

    public boolean isSupervisor() {
        return sessionManager.isSupervisor();
    }

    public boolean isFieldWorker() {
        return sessionManager.isFieldWorker();
    }
}

package com.example.nutrimap.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.ActivityMainBinding;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;

/**
 * Main activity hosting the navigation drawer and fragment container.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get user email from intent
        currentUserEmail = getIntent().getStringExtra("user_email");
        if (currentUserEmail == null) {
            currentUserEmail = "admin@nutrimap.com";
        }

        setupToolbar();
        setupNavigation();
        updateNavHeader();
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

        // Configure top-level destinations (no back button)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.childrenFragment,
                R.id.visitsFragment,
                R.id.usersFragment,
                R.id.branchesFragment,
                R.id.profileFragment
        ).setOpenableLayout(binding.drawerLayout).build();

        // Setup ActionBar with Navigation
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Handle navigation item selection
        binding.navigationView.setNavigationItemSelectedListener(this);

        // Setup drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.nav_home, R.string.nav_home
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void updateNavHeader() {
        View headerView = binding.navigationView.getHeaderView(0);
        TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
        TextView textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

        User user = UserRepository.getInstance().getUserByEmail(currentUserEmail);
        if (user != null) {
            textViewUserName.setText(user.getName());
            textViewUserEmail.setText(user.getEmail());
        } else {
            textViewUserName.setText("NutriMap User");
            textViewUserEmail.setText(currentUserEmail);
        }
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
        return currentUserEmail;
    }
}

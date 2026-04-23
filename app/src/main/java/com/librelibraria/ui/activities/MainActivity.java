package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.librelibraria.R;
import com.librelibraria.ui.fragments.CatalogFragment;
import com.librelibraria.ui.fragments.DashboardFragment;
import com.librelibraria.ui.fragments.LendingFragment;
import com.librelibraria.ui.fragments.MoreFragment;

/**
 * Main activity with bottom navigation.
 */
public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigation;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(this);
        bottomNavigation.setLabelVisibilityMode(androidx.navigation.BottomNavigationView.LABEL_VISIBILITY_LABELED);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_dashboard) {
            loadFragment(new DashboardFragment());
            return true;
        } else if (itemId == R.id.nav_catalog) {
            loadFragment(new CatalogFragment());
            return true;
        } else if (itemId == R.id.nav_lending) {
            loadFragment(new LendingFragment());
            return true;
        } else if (itemId == R.id.nav_more) {
            loadFragment(new MoreFragment());
            return true;
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof DashboardFragment) {
            super.onBackPressed();
        } else {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }
}

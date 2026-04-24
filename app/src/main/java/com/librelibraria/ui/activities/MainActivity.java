package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.librelibraria.R;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.ui.fragments.CatalogFragment;
import com.librelibraria.ui.fragments.DashboardFragment;
import com.librelibraria.ui.fragments.LendingFragment;
import com.librelibraria.ui.fragments.BorrowersFragment;
import com.librelibraria.ui.fragments.MoreFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigation;
    private NavigationRailView navigationRail;
    private Fragment currentFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupNavigation();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        navigationRail = findViewById(R.id.navigation_rail);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    private void setupNavigation() {
        if (bottomNavigation != null && bottomNavigation.getVisibility() == View.VISIBLE) {
            bottomNavigation.setOnItemSelectedListener(this);
        } else if (navigationRail != null && navigationRail.getVisibility() == View.VISIBLE) {
            navigationRail.setOnItemSelectedListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            navigateToCatalog();
            return true;
        } else if (id == R.id.action_sync) {
            performSync();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToCatalog() {
        loadFragment(new CatalogFragment());
    }

    private void performSync() {
        LibreLibrariaApp app = (LibreLibrariaApp) getApplication();
        app.getSyncManager().sync();
        Toast.makeText(this, R.string.sync_complete, Toast.LENGTH_SHORT).show();
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
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
        } else if (itemId == R.id.nav_borrowers) {
            loadFragment(new BorrowersFragment());
            return true;
        } else if (itemId == R.id.nav_more) {
            loadFragment(new MoreFragment());
            return true;
        }
        return false;
    }
}
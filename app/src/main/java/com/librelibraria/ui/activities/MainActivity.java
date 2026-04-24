package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.librelibraria.R;
import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.ui.fragments.CatalogFragment;
import com.librelibraria.ui.fragments.DashboardFragment;
import com.librelibraria.ui.fragments.DiaryFragment;
import com.librelibraria.ui.fragments.LendingFragment;
import com.librelibraria.ui.fragments.BorrowersFragment;
import com.librelibraria.ui.fragments.MoreFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    // Navigation order — must match menu_bottom_navigation.xml
    private static final int[] NAV_IDS = {
            R.id.nav_dashboard,
            R.id.nav_catalog,
            R.id.nav_diary,
            R.id.nav_lending,
            R.id.nav_borrowers,
            R.id.nav_more
    };

    // Swipe thresholds
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH  = 250;
    private static final int SWIPE_MIN_VELOCITY  = 200;

    private BottomNavigationView  bottomNavigation;
    private NavigationRailView    navigationRail;
    private Fragment              currentFragment;
    private Toolbar               toolbar;
    private GestureDetectorCompat gestureDetector;
    private int                   currentTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupNavigation();
        setupGestureDetector();

        if (savedInstanceState == null) {
            navigateToTab(0, 0);
        }
    }

    private void initViews() {
        toolbar          = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        navigationRail   = findViewById(R.id.navigation_rail);
    }

    private void setupToolbar() {
        if (toolbar != null) setSupportActionBar(toolbar);
    }

    private void setupNavigation() {
        if (bottomNavigation != null && bottomNavigation.getVisibility() == View.VISIBLE) {
            bottomNavigation.setOnItemSelectedListener(this);
        } else if (navigationRail != null && navigationRail.getVisibility() == View.VISIBLE) {
            navigationRail.setOnItemSelectedListener(this);
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    // Navigate to tab by index. direction: +1 = forward, -1 = back, 0 = neutral
    private void navigateToTab(int index, int direction) {
        if (index < 0 || index >= NAV_IDS.length) return;
        Fragment fragment = fragmentForIndex(index);
        if (fragment == null) return;

        int animIn, animOut;
        if (direction > 0) {
            animIn  = R.anim.slide_in_right;
            animOut = R.anim.slide_out_left;
        } else if (direction < 0) {
            animIn  = R.anim.slide_in_left;
            animOut = R.anim.slide_out_right;
        } else {
            animIn  = R.anim.slide_in_bottom;
            animOut = R.anim.fade_out;
        }

        currentTabIndex = index;
        currentFragment = fragment;

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(animIn, animOut);
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();

        syncNavSelection(NAV_IDS[index]);
    }

    private void syncNavSelection(int menuId) {
        if (bottomNavigation != null && bottomNavigation.getVisibility() == View.VISIBLE) {
            bottomNavigation.setOnItemSelectedListener(null);
            bottomNavigation.setSelectedItemId(menuId);
            bottomNavigation.setOnItemSelectedListener(this);
        } else if (navigationRail != null && navigationRail.getVisibility() == View.VISIBLE) {
            navigationRail.setOnItemSelectedListener(null);
            navigationRail.setSelectedItemId(menuId);
            navigationRail.setOnItemSelectedListener(this);
        }
    }

    private static Fragment fragmentForIndex(int index) {
        switch (index) {
            case 0: return new DashboardFragment();
            case 1: return new CatalogFragment();
            case 2: return new DiaryFragment();
            case 3: return new LendingFragment();
            case 4: return new BorrowersFragment();
            case 5: return new MoreFragment();
            default: return null;
        }
    }

    private static int indexForNavId(int navId) {
        for (int i = 0; i < NAV_IDS.length; i++) {
            if (NAV_IDS[i] == navId) return i;
        }
        return -1;
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
            navigateToTab(indexForNavId(R.id.nav_catalog), 0);
            return true;
        } else if (id == R.id.action_sync) {
            LibreLibrariaApp app = (LibreLibrariaApp) getApplication();
            app.getSyncManager().sync();
            Toast.makeText(this, R.string.sync_complete, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int index = indexForNavId(item.getItemId());
        if (index < 0) return false;
        int direction = Integer.compare(index, currentTabIndex);
        navigateToTab(index, direction);
        return true;
    }

    // Swipe gesture: left = next tab, right = previous tab
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            if (e1 == null) return false;

            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();

            // Ignore if mostly vertical (user is scrolling a list)
            if (Math.abs(deltaY) > SWIPE_MAX_OFF_PATH) return false;

            if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {

                if (deltaX < 0 && currentTabIndex < NAV_IDS.length - 1) {
                    // Swipe left → next tab
                    navigateToTab(currentTabIndex + 1, +1);
                    return true;
                } else if (deltaX > 0 && currentTabIndex > 0) {
                    // Swipe right → previous tab
                    navigateToTab(currentTabIndex - 1, -1);
                    return true;
                }
            }
            return false;
        }
    }
}
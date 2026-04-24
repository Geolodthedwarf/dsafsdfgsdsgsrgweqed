package com.librelibraria.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.librelibraria.R;
import com.librelibraria.data.model.IntroPage;
import com.librelibraria.data.storage.FileStorageManager;
import com.librelibraria.ui.adapterutil.IntroPagerAdapter;
import com.librelibraria.ui.util.AppAnimations;
import java.io.File;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private MaterialButton btnSkip, btnNext;
    private IntroPagerAdapter adapter;
    private int currentPage = 0;

    private FileStorageManager storageManager;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        storageManager = FileStorageManager.getInstance(this);

        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            final int flags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            try {
                                // Take persistable permission - this is critical for SAF
                                getContentResolver().takePersistableUriPermission(treeUri, flags);
                            } catch (SecurityException e) {
                                // Some providers don't support persistable permissions
                                // Continue anyway - we might still be able to use the folder temporarily
                                android.util.Log.w("IntroActivity", "Could not persist URI permission: " + e.getMessage());
                            }

                            // Verify we have write permission before proceeding
                            boolean hasPermission = (flags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0;
                            if (hasPermission && storageManager.initializeFromSelectedTree(treeUri)) {
                                Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
                                finishIntro();
                            } else {
                                // Fallback: try to proceed anyway, or use default folder
                                android.util.Log.w("IntroActivity", "Could not initialize storage from selected folder, using default");
                                useDefaultFolder();
                            }
                        } else {
                            android.util.Log.w("IntroActivity", "No URI returned from folder picker");
                            useDefaultFolder();
                        }
                    } else {
                        // User cancelled or picker returned an error
                        android.util.Log.i("IntroActivity", "Folder picker cancelled or returned error");
                        useDefaultFolder();
                    }
                }
        );

        initViews();
        setupViewPager();
        setupButtons();
    }

    private String getPathFromUri(Uri uri) {
        if (uri == null) return null;

        if ("content".equals(uri.getScheme())) {
            try {
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String fileName = cursor.getString(index);
                        File externalDir = getExternalFilesDir(null);
                        if (externalDir != null) {
                            return externalDir.getAbsolutePath();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return uri.getPath();
    }

    private void useDefaultFolder() {
        try {
            File baseDir = getFilesDir();
            if (baseDir != null) {
                // First, try using internal storage as the base
                String path = baseDir.getAbsolutePath();
                storageManager.setBasePath(path);
                storageManager.setBaseUri(null); // Clear any SAF URI

                boolean created = storageManager.createBaseFolders();
                if (created) {
                    Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
                    finishIntro();
                } else {
                    // Try direct creation
                    boolean success = tryDirectCreation(baseDir);
                    if (!success) {
                        tryInternalStorage();
                    }
                }
            } else {
                tryInternalStorage();
            }
        } catch (Exception e) {
            android.util.Log.e("IntroActivity", "useDefaultFolder exception: " + e.getMessage(), e);
            tryInternalStorage();
        }
    }

    private boolean tryDirectCreation(File baseDir) {
        try {
            if (baseDir == null) {
                android.util.Log.w("IntroActivity", "tryDirectCreation: baseDir is null");
                return false;
            }

            File libreDir = new File(baseDir, "LibreLibraria");
            if (!libreDir.exists()) {
                boolean created = libreDir.mkdirs();
                if (!created) {
                    created = createFolderManually(libreDir);
                }
                if (!created) {
                    android.util.Log.w("IntroActivity", "Could not create LibreLibraria folder at: " + libreDir.getAbsolutePath());
                    return false;
                }
            }
            if (libreDir.exists() && libreDir.isDirectory()) {
                storageManager.setBasePath(libreDir.getAbsolutePath());
                storageManager.setBaseUri(null); // Clear any SAF URI
                boolean subFoldersCreated = createSubFoldersManually(libreDir);
                if (subFoldersCreated) {
                    android.util.Log.i("IntroActivity", "Created storage at: " + libreDir.getAbsolutePath());
                    return true;
                } else {
                    // Even if subfolders fail, we can still proceed
                    android.util.Log.w("IntroActivity", "Some subfolders could not be created, but proceeding anyway");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("IntroActivity", "tryDirectCreation exception: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean createFolderManually(File folder) {
        try {
            if (folder.exists()) return true;
            boolean result = folder.mkdir();
            if (!result) {
                result = folder.mkdirs();
            }
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean createSubFoldersManually(File baseDir) {
        if (baseDir == null || !baseDir.exists() || !baseDir.isDirectory()) {
            return false;
        }

        String[] subs = {"books", "loans", "borrowers", "tags", "themes", "library"};
        boolean allCreated = true;
        for (String sub : subs) {
            File folder = new File(baseDir, sub);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    created = folder.mkdir();
                }
                if (!created) {
                    android.util.Log.w("IntroActivity", "Could not create subfolder: " + sub);
                    allCreated = false;
                }
            }
        }
        return allCreated;
    }

    private void tryInternalStorage() {
        try {
            File baseDir = getFilesDir();
            if (baseDir == null) {
                android.util.Log.e("IntroActivity", "tryInternalStorage: getFilesDir() returned null");
                // Set a minimal fallback
                storageManager.setBasePath("/data/data/" + getPackageName() + "/files/LibreLibraria");
                storageManager.setBaseUri(null);
            } else {
                File libreDir = new File(baseDir, "LibreLibraria");
                createSubFoldersManually(libreDir);
                storageManager.setBasePath(libreDir.getAbsolutePath());
                storageManager.setBaseUri(null);
            }
            Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("IntroActivity", "tryInternalStorage exception: " + e.getMessage(), e);
            // Best-effort: set a fallback path even if folder creation failed,
            // so we don't trap the user on the intro screen forever.
            try {
                File fallback = getFilesDir();
                if (fallback != null) {
                    String fallbackPath = new File(fallback, "LibreLibraria").getAbsolutePath();
                    storageManager.setBasePath(fallbackPath);
                    storageManager.setBaseUri(null);
                    android.util.Log.i("IntroActivity", "Using fallback storage path: " + fallbackPath);
                }
            } catch (Exception ignored) {
                android.util.Log.e("IntroActivity", "Even fallback storage failed", ignored);
            }
        }
        // Always proceed to the main app — do NOT leave the user stuck here.
        finishIntro();
    }

    private void showFolderSelection() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        folderPickerLauncher.launch(intent);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewpager);
        indicatorLayout = findViewById(R.id.indicator_layout);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        adapter = new IntroPagerAdapter(IntroPage.getPages());
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateIndicator();
                updateButtons();
            }
        });
    }

    private void setupViewPager() {
        int pageCount = adapter.getItemCount();
        indicatorLayout.removeAllViews();

        for (int i = 0; i < pageCount; i++) {
            View indicator = new View(this);
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(size / 2, 0, size / 2, 0);
            indicator.setLayoutParams(params);
            indicatorLayout.addView(indicator);
        }
        updateIndicator();
    }

    private void updateIndicator() {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View child = indicatorLayout.getChildAt(i);
            child.setBackgroundResource(i == currentPage ?
                    R.drawable.indicator_active : R.drawable.indicator_inactive);
        }
    }

    private void updateButtons() {
        if (currentPage == adapter.getItemCount() - 1) {
            btnNext.setText(R.string.finish);
            btnSkip.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setText(R.string.next);
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == adapter.getItemCount() - 1) {
                showFolderExplanationDialog();
            } else {
                viewPager.setCurrentItem(currentPage + 1, true);
            }
        });

        btnSkip.setOnClickListener(v -> showFolderExplanationDialog());
    }

    private void showFolderExplanationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setMessage("Choose where to store your library data.\n\nYou can pick a custom folder (recommended) so your books are saved to a location of your choice, or skip to use the app's private internal storage.")
                .setPositiveButton("Choose folder", (d, w) -> showFolderSelection())
                .setNegativeButton("Use default", (d, w) -> useDefaultFolder())
                .setCancelable(false)
                .show();
    }

    private void finishIntro() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit().putBoolean("intro_completed", true).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppAnimations.fadeIn(findViewById(android.R.id.content));
    }
}
package com.librelibraria.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
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
                        getContentResolver().takePersistableUriPermission(treeUri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | 
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        
                        storageManager.setBaseUri(treeUri.toString());
                        
                        if (storageManager.createLibreLibrariaFolder(treeUri)) {
                            String path = "LibreLibraria";
                            storageManager.setBasePath(path);
                            storageManager.createBaseFolders();
                            Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
                            finishIntro();
                        } else {
                            useDefaultFolder();
                        }
                    } else {
                        useDefaultFolder();
                    }
                } else {
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
                String path = baseDir.getAbsolutePath() + "/LibreLibraria";
                storageManager.setBasePath(path);
                boolean created = storageManager.createBaseFolders();
                if (created) {
                    Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
                    finishIntro();
                } else {
                    tryDirectCreation(baseDir);
                }
            } else {
                tryDirectCreation(getFilesDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
            tryInternalStorage();
        }
    }
    
    private void tryDirectCreation(File baseDir) {
        try {
            File libreDir = new File(baseDir, "LibreLibraria");
            if (!libreDir.exists()) {
                boolean created = libreDir.mkdirs();
                if (!created) {
                    created = createFolderManually(libreDir);
                }
            }
            if (libreDir.exists() || createFolderManually(libreDir)) {
                storageManager.setBasePath(libreDir.getAbsolutePath());
                createSubFoldersManually(libreDir);
                Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
                finishIntro();
            } else {
                tryInternalStorage();
            }
        } catch (Exception e) {
            tryInternalStorage();
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
    
    private void createSubFoldersManually(File baseDir) {
        String[] subs = {"books", "loans", "borrowers", "tags", "themes", "library"};
        for (String sub : subs) {
            File folder = new File(baseDir, sub);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    created = folder.mkdir();
                }
            }
        }
    }
    
    private void tryInternalStorage() {
        try {
            File baseDir = getFilesDir();
            File libreDir = new File(baseDir, "LibreLibraria");
            createSubFoldersManually(libreDir);
            storageManager.setBasePath(libreDir.getAbsolutePath());
            Toast.makeText(this, R.string.folder_selected, Toast.LENGTH_SHORT).show();
            finishIntro();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_creating_folders, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showFolderSelection() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
                showFolderSelection();
            } else {
                viewPager.setCurrentItem(currentPage + 1, true);
            }
        });
        
        btnSkip.setOnClickListener(v -> showFolderSelection());
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
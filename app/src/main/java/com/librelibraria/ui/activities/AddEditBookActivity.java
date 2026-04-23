package com.librelibraria.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanOptions;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.model.Tag;
import com.librelibraria.ui.viewmodels.AddEditBookViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for adding or editing a book.
 */
public class AddEditBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    private Toolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etAuthor;
    private TextInputEditText etIsbn;
    private TextInputEditText etPublisher;
    private TextInputEditText etYear;
    private TextInputEditText etCopies;
    private TextInputEditText etShelfLocation;
    private TextInputEditText etDescription;
    private AutoCompleteTextView actvGenre;
    private AutoCompleteTextView actvLanguage;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupTags;
    private Button btnScanIsbn;
    private View progressBar;

    private AddEditBookViewModel viewModel;
    private long bookId = -1;
    private boolean isEditMode = false;
    private List<Tag> availableTags = new ArrayList<>();
    private List<String> selectedTags = new ArrayList<>();
    private String selectedStatus = ReadingStatus.OWN.name();

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new com.journeyapps.barcodescanner.ScanContract(), result -> {
                if (result != null && result.getContents() != null) {
                    etIsbn.setText(result.getContents());
                    viewModel.lookupIsbn(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        isEditMode = bookId > 0;

        initViews();
        setupToolbar();
        setupViewModel();
        setupListeners();
        loadInitialData();

        if (isEditMode) {
            viewModel.loadBook(bookId);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etAuthor = findViewById(R.id.et_author);
        etIsbn = findViewById(R.id.et_isbn);
        etPublisher = findViewById(R.id.et_publisher);
        etYear = findViewById(R.id.et_year);
        etCopies = findViewById(R.id.et_copies);
        etShelfLocation = findViewById(R.id.et_shelf_location);
        etDescription = findViewById(R.id.et_description);
        actvGenre = findViewById(R.id.actv_genre);
        actvLanguage = findViewById(R.id.actv_language);
        chipGroupStatus = findViewById(R.id.chip_group_status);
        chipGroupTags = findViewById(R.id.chip_group_tags);
        btnScanIsbn = findViewById(R.id.btn_scan_isbn);
        progressBar = findViewById(R.id.progress_bar);

        // Setup dropdown adapters
        String[] languages = {"Ukrainian", "English", "German", "French", "Spanish", "Russian", "Other"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, languages);
        actvLanguage.setAdapter(languageAdapter);
        actvLanguage.setText("Ukrainian", false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(isEditMode ? R.string.edit_book : R.string.add_book);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddEditBookViewModel.class);

        viewModel.getBook().observe(this, book -> {
            if (book != null) {
                populateForm(book);
            }
        });

        viewModel.getGenres().observe(this, genres -> {
            if (genres != null && !genres.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, genres);
                actvGenre.setAdapter(adapter);
            }
        });

        viewModel.getTags().observe(this, tags -> {
            if (tags != null) {
                availableTags = tags;
                setupTagChips();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void setupListeners() {
        btnScanIsbn.setOnClickListener(v -> scanBarcode());

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    selectedStatus = chip.getTag().toString();
                }
            }
        });
    }

    private void loadInitialData() {
        viewModel.loadGenres();
        viewModel.loadTags();
    }

    private void populateForm(Book book) {
        etTitle.setText(book.getTitle());
        etAuthor.setText(book.getAuthor());
        etIsbn.setText(book.getIsbn());
        etPublisher.setText(book.getPublisher());
        etYear.setText(book.getPublishYear());
        etCopies.setText(String.valueOf(book.getCopies()));
        etShelfLocation.setText(book.getShelfLocation());
        etDescription.setText(book.getDescription());
        actvGenre.setText(book.getGenre(), false);
        actvLanguage.setText(book.getLanguage(), false);

        selectedStatus = book.getReadingStatus() != null ?
                book.getReadingStatus().name() : ReadingStatus.OWN.name();

        // Select status chip
        for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStatus.getChildAt(i);
            if (chip.getTag().toString().equals(selectedStatus)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void setupTagChips() {
        chipGroupTags.removeAllViews();
        for (Tag tag : availableTags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCheckable(true);
            chip.setTag(tag.getName());

            if (selectedTags.contains(tag.getName())) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedTags.contains(tag.getName())) {
                        selectedTags.add(tag.getName());
                    }
                } else {
                    selectedTags.remove(tag.getName());
                }
            });

            chipGroupTags.addView(chip);
        }
    }

    private void scanBarcode() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.EAN_13, ScanOptions.EAN_8, ScanOptions.UPC_A);
        options.setPrompt(getString(R.string.scan_isbn_prompt));
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(false);

        scanLauncher.launch(options);
    }

    private void saveBook() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_title_required));
            return;
        }

        Book book = isEditMode && viewModel.getBook().getValue() != null ?
                viewModel.getBook().getValue() : new Book();

        book.setTitle(title);
        book.setAuthor(etAuthor.getText() != null ? etAuthor.getText().toString().trim() : "");
        book.setIsbn(etIsbn.getText() != null ? etIsbn.getText().toString().trim() : "");
        book.setPublisher(etPublisher.getText() != null ? etPublisher.getText().toString().trim() : "");
        book.setPublishYear(etYear.getText() != null ? etYear.getText().toString().trim() : "");
        book.setGenre(actvGenre.getText() != null ? actvGenre.getText().toString().trim() : "");
        book.setLanguage(actvLanguage.getText() != null ? actvLanguage.getText().toString().trim() : "Ukrainian");
        book.setShelfLocation(etShelfLocation.getText() != null ? etShelfLocation.getText().toString().trim() : "");
        book.setDescription(etDescription.getText() != null ? etDescription.getText().toString().trim() : "");
        book.setReadingStatus(ReadingStatus.fromString(selectedStatus));
        book.setTags(String.join(",", selectedTags));

        try {
            book.setCopies(Integer.parseInt(etCopies.getText().toString().trim()));
        } catch (NumberFormatException e) {
            book.setCopies(1);
        }

        if (isEditMode) {
            viewModel.updateBook(book);
        } else {
            viewModel.saveBook(book);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveBook();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

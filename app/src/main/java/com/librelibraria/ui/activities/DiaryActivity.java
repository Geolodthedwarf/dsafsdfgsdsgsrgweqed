package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.librelibraria.R;
import com.librelibraria.data.model.DiaryEntry;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.repository.BookRepository;
import com.librelibraria.ui.adapters.DiaryAdapter;
import com.librelibraria.ui.viewmodels.DiaryViewModel;

/**
 * Activity for managing reading diary entries.
 */
public class DiaryActivity extends AppCompatActivity implements DiaryAdapter.OnDiaryEntryClickListener {

    private Toolbar toolbar;
    private RecyclerView rvDiary;
    private View emptyView;
    private View progressBar;

    private DiaryAdapter adapter;
    private DiaryViewModel viewModel;
    private BookRepository bookRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupListeners();

        bookRepository = ((com.librelibraria.LibreLibrariaApp) getApplication()).getBookRepository();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvDiary = findViewById(R.id.recycler_diary);
        emptyView = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.reading_diary);
    }

    private void setupRecyclerView() {
        adapter = new DiaryAdapter(this);
        rvDiary.setLayoutManager(new LinearLayoutManager(this));
        rvDiary.setAdapter(adapter);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                DiaryEntry entry = adapter.getEntryAt(position);
                if (entry != null) {
                    confirmDelete(entry, position);
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvDiary);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);

        viewModel.getDiaryEntries().observe(this, entries -> {
            if (entries != null) {
                adapter.submitList(entries);
                emptyView.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                rvDiary.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_add_entry);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddEditDialog(null));
        }
    }

    private void showAddEditDialog(DiaryEntry existingEntry) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_diary_entry, null);

        AutoCompleteTextView etBookTitle = dialogView.findViewById(R.id.et_book_title);
        TextInputEditText etNote = dialogView.findViewById(R.id.et_note);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_date);
        TextInputEditText etQuote = dialogView.findViewById(R.id.et_quote);

        // Pre-fill if editing
        if (existingEntry != null) {
            etBookTitle.setText(existingEntry.getBookTitle());
            etNote.setText(existingEntry.getNote());
            etQuote.setText(existingEntry.getQuote());

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            etDate.setText(sdf.format(new java.util.Date(existingEntry.getDate())));
        }

        String title = existingEntry != null ? getString(R.string.edit_note) : getString(R.string.add_note);

        final java.util.List<com.librelibraria.data.model.Book>[] readingBooksHolder = new java.util.List[]{new java.util.ArrayList<>()};
        // Populate dropdown with currently reading books (Room)
        if (bookRepository != null) {
            bookRepository.getBooksByReadingStatus(ReadingStatus.READING)
                    .firstOrError()
                    .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(
                            books -> {
                                readingBooksHolder[0] = books;
                                java.util.List<String> titles = new java.util.ArrayList<>();
                                for (var b : books) {
                                    if (b.getTitle() != null && !b.getTitle().isEmpty()) titles.add(b.getTitle());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
                                etBookTitle.setAdapter(adapter);
                            },
                            error -> {}
                    );
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String bookTitle = etBookTitle.getText() != null ? etBookTitle.getText().toString().trim() : "";
                    String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
                    String quote = etQuote.getText() != null ? etQuote.getText().toString().trim() : "";
                    String dateStr = etDate.getText() != null ? etDate.getText().toString().trim() : "";

                    if (bookTitle.isEmpty()) {
                        return;
                    }

                    long selectedBookId = -1;
                    for (var b : readingBooksHolder[0]) {
                        if (b.getTitle() != null && b.getTitle().equals(bookTitle)) {
                            selectedBookId = b.getId();
                            break;
                        }
                    }
                    if (selectedBookId <= 0) {
                        // Must pick one of the currently reading books
                        return;
                    }

                    long date;
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        date = sdf.parse(dateStr).getTime();
                    } catch (Exception e) {
                        date = System.currentTimeMillis();
                    }

                    if (existingEntry != null) {
                        existingEntry.setBookId(selectedBookId);
                        existingEntry.setBookTitle(bookTitle);
                        existingEntry.setNote(note);
                        existingEntry.setQuote(quote);
                        existingEntry.setDate(date);
                        existingEntry.setSynced(false);
                        viewModel.updateEntry(existingEntry);
                    } else {
                        DiaryEntry newEntry = new DiaryEntry();
                        newEntry.setBookId(selectedBookId);
                        newEntry.setBookTitle(bookTitle);
                        newEntry.setNote(note);
                        newEntry.setQuote(quote);
                        newEntry.setDate(date);
                        viewModel.addEntry(newEntry);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(DiaryEntry entry, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_note)
                .setMessage(getString(R.string.delete_note_confirm, entry.getBookTitle()))
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteEntry(entry))
                .setNegativeButton(R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    @Override
    public void onEntryClick(DiaryEntry entry) {
        showAddEditDialog(entry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_diary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        String[] options = {
                getString(R.string.all_entries),
                getString(R.string.this_week),
                getString(R.string.this_month)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.filter)
                .setItems(options, (dialog, which) -> {
                    long startDate = 0;
                    switch (which) {
                        case 1: // This week
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                            cal.set(java.util.Calendar.MINUTE, 0);
                            cal.set(java.util.Calendar.SECOND, 0);
                            startDate = cal.getTimeInMillis();
                            break;
                        case 2: // This month
                            cal = java.util.Calendar.getInstance();
                            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                            cal.set(java.util.Calendar.MINUTE, 0);
                            cal.set(java.util.Calendar.SECOND, 0);
                            startDate = cal.getTimeInMillis();
                            break;
                    }
                    viewModel.filterByDate(startDate);
                })
                .show();
    }
}

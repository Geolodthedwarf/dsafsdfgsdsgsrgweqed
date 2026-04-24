package com.librelibraria.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.ui.dialogs.RatingDialog;
import com.librelibraria.ui.util.OpenLibraryCover;
import com.librelibraria.ui.viewmodels.BookDetailViewModel;

/**
 * Activity showing book details.
 */
public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    private Toolbar toolbar;
    private ImageView ivCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvIsbn;
    private TextView tvPublisher;
    private TextView tvYear;
    private TextView tvGenre;
    private TextView tvLanguage;
    private TextView tvCopies;
    private TextView tvShelf;
    private TextView tvStatus;
    private TextView tvDescription;
    private TextView tvReadingStatus;
    private RatingBar ratingBar;
    private View availabilityCard;
    private View lendingCard;

    private BookDetailViewModel viewModel;
    private long bookId;
    private ActivityResultLauncher<String[]> coverPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        if (bookId < 0) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupListeners();

        coverPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                    }
                    viewModel.updateCover(uri.toString());
                }
        );
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivCover = findViewById(R.id.iv_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvIsbn = findViewById(R.id.tv_isbn);
        tvPublisher = findViewById(R.id.tv_publisher);
        tvYear = findViewById(R.id.tv_year);
        tvGenre = findViewById(R.id.tv_genre);
        tvLanguage = findViewById(R.id.tv_language);
        tvCopies = findViewById(R.id.tv_copies);
        tvShelf = findViewById(R.id.tv_shelf);
        tvStatus = findViewById(R.id.tv_status);
        tvDescription = findViewById(R.id.tv_description);
        tvReadingStatus = findViewById(R.id.tv_reading_status);
        ratingBar = findViewById(R.id.rating_bar);
        availabilityCard = findViewById(R.id.availability_card);
        lendingCard = findViewById(R.id.lending_card);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BookDetailViewModel.class);

        viewModel.getBook().observe(this, book -> {
            if (book != null) {
                displayBook(book);
            }
        });

        viewModel.getActiveLoan().observe(this, loan -> {
            if (loan != null && loan.getStatus() == LoanStatus.ACTIVE) {
                lendingCard.setVisibility(View.VISIBLE);
                // Show borrower info
            } else {
                lendingCard.setVisibility(View.GONE);
            }
        });

        viewModel.getDeleteSuccess().observe(this, success -> {
            if (success != null && success) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void setupListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_action);
        if (fab != null) {
            fab.setOnClickListener(v -> showActionDialog());
        }

        ivCover.setOnClickListener(v -> {
            // Let user pick a cover when OpenLibrary cover isn't available or they want a custom one.
            coverPicker.launch(new String[]{"image/*"});
        });

        availabilityCard.setOnClickListener(v -> {
            // Toggle availability or edit copies
        });

        ratingBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Book book = viewModel.getBook().getValue();
                float current = book != null ? (float) book.getRating() : 0f;
                showRatingDialog(current);
            }
            return true; // consume, dialog controls the rating
        });
    }

    private void displayBook(Book book) {
        getSupportActionBar().setTitle(book.getTitle());

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "-");
        tvIsbn.setText(book.getIsbn() != null ? book.getIsbn() : "-");
        tvPublisher.setText(book.getPublisher() != null ? book.getPublisher() : "-");
        tvYear.setText(book.getPublishYear() != null ? book.getPublishYear() : "-");
        tvGenre.setText(book.getGenre() != null ? book.getGenre() : "-");
        tvLanguage.setText(book.getLanguage() != null ? book.getLanguage() : "-");
        tvCopies.setText(getString(R.string.copies_format, book.getAvailableCopies(), book.getCopies()));
        tvShelf.setText(book.getShelfLocation() != null ? book.getShelfLocation() : "-");
        tvDescription.setText(book.getDescription() != null ? book.getDescription() : getString(R.string.no_description));

        if (book.getReadingStatus() != null) {
            tvReadingStatus.setText(book.getReadingStatus().getFullDisplay());
        }

        if (book.isAvailable()) {
            tvStatus.setText(R.string.available);
            tvStatus.setTextColor(getColor(R.color.status_available));
        } else {
            tvStatus.setText(R.string.borrowed);
            tvStatus.setTextColor(getColor(R.color.status_borrowed));
        }

        ratingBar.setRating((float) book.getRating());
        ratingBar.setVisibility(View.VISIBLE);

        // Load cover image
        String coverUrl = book.getCustomCoverUrl();
        if (coverUrl == null || coverUrl.isEmpty()) {
            coverUrl = OpenLibraryCover.large(book.getIsbn());
        }
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivCover);
        } else {
            ivCover.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    private void showActionDialog() {
        Book book = viewModel.getBook().getValue();
        if (book == null) return;

        String[] actions;
        if (book.isAvailable()) {
            actions = new String[]{getString(R.string.lend_book), getString(R.string.edit_book), getString(R.string.delete_book)};
        } else {
            actions = new String[]{getString(R.string.return_book), getString(R.string.edit_book), getString(R.string.delete_book)};
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.actions)
                .setItems(actions, (dialog, which) -> {
                    if (book.isAvailable()) {
                        if (which == 0) lendBook();
                        else if (which == 1) editBook();
                        else if (which == 2) confirmDelete();
                    } else {
                        if (which == 0) returnBook();
                        else if (which == 1) editBook();
                        else if (which == 2) confirmDelete();
                    }
                })
                .show();
    }

    private void showRatingDialog(float currentRating) {
        RatingDialog dialog = new RatingDialog();
        dialog.setInitialRating(currentRating);
        dialog.setOnRatingSavedListener((rating, review) -> {
            viewModel.saveRating(rating, review);
        });
        dialog.show(getSupportFragmentManager(), "RatingDialog");
    }

    private void lendBook() {
        // Launch lending dialog
    }

    private void returnBook() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.return_book)
                .setMessage(R.string.confirm_return)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    viewModel.returnBook();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void editBook() {
        Intent intent = new Intent(this, AddEditBookActivity.class);
        intent.putExtra(AddEditBookActivity.EXTRA_BOOK_ID, bookId);
        startActivity(intent);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_book)
                .setMessage(R.string.delete_book_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteBook();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            editBook();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadBook(bookId);
    }
}

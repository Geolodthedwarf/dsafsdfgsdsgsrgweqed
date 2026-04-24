package com.librelibraria.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.librelibraria.R;
import com.librelibraria.data.model.DiaryEntry;
import com.librelibraria.ui.adapters.DiaryAdapter;
import com.librelibraria.ui.viewmodels.DiaryViewModel;

public class DiaryFragment extends Fragment implements DiaryAdapter.OnDiaryEntryClickListener {

    private RecyclerView rvDiary;
    private View emptyView;
    private View progressBar;
    private DiaryAdapter adapter;
    private DiaryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDiary = view.findViewById(R.id.recycler_diary);
        emptyView = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);

        adapter = new DiaryAdapter(this);
        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDiary.setAdapter(adapter);

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

        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        viewModel.getDiaryEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries == null) return;
            adapter.submitList(entries);
            emptyView.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
            rvDiary.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        View fab = view.findViewById(R.id.fab_add_entry);
        if (fab != null) fab.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void showAddEditDialog(DiaryEntry existingEntry) {
        // Reuse the DiaryActivity dialog layout/logic for now.
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_diary_entry, null);

        TextInputEditText etBookTitle = dialogView.findViewById(R.id.et_book_title);
        TextInputEditText etNote = dialogView.findViewById(R.id.et_note);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_date);
        TextInputEditText etQuote = dialogView.findViewById(R.id.et_quote);

        if (existingEntry != null) {
            etBookTitle.setText(existingEntry.getBookTitle());
            etNote.setText(existingEntry.getNote());
            etQuote.setText(existingEntry.getQuote());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            etDate.setText(sdf.format(new java.util.Date(existingEntry.getDate())));
        }

        String title = existingEntry != null ? getString(R.string.edit_note) : getString(R.string.add_note);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String bookTitle = etBookTitle.getText() != null ? etBookTitle.getText().toString().trim() : "";
                    if (bookTitle.isEmpty()) return;

                    String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
                    String quote = etQuote.getText() != null ? etQuote.getText().toString().trim() : "";
                    String dateStr = etDate.getText() != null ? etDate.getText().toString().trim() : "";

                    long date;
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        date = sdf.parse(dateStr).getTime();
                    } catch (Exception e) {
                        date = System.currentTimeMillis();
                    }

                    if (existingEntry != null) {
                        existingEntry.setBookTitle(bookTitle);
                        existingEntry.setNote(note);
                        existingEntry.setQuote(quote);
                        existingEntry.setDate(date);
                        existingEntry.setSynced(false);
                        viewModel.updateEntry(existingEntry);
                    } else {
                        DiaryEntry newEntry = new DiaryEntry();
                        // NOTE: bookId will be set once we wire dropdown selection (todo: nav-diary)
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
        new MaterialAlertDialogBuilder(requireContext())
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
}


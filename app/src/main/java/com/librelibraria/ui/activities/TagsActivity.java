package com.librelibraria.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Tag;
import com.librelibraria.ui.viewmodels.TagsViewModel;

import java.util.List;

/**
 * Activity for managing library tags.
 */
public class TagsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ChipGroup chipGroupTags;
    private View emptyView;
    private View progressBar;

    private TagsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        initViews();
        setupToolbar();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupTags = findViewById(R.id.chip_group_tags);
        emptyView = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.tags);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TagsViewModel.class);

        viewModel.getTags().observe(this, tags -> {
            if (tags != null) {
                updateChipGroup(tags);
                emptyView.setVisibility(tags.isEmpty() ? View.VISIBLE : View.GONE);
                chipGroupTags.setVisibility(tags.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getTagDeleted().observe(this, deleted -> {
            if (deleted != null && deleted) {
                // Tag was deleted successfully
            }
        });
    }

    private void setupListeners() {
        FloatingActionButton fab = findViewById(R.id.fab_add_tag);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddTagDialog());
        }
    }

    private void updateChipGroup(List<Tag> tags) {
        chipGroupTags.removeAllViews();

        for (Tag tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCloseIconVisible(true);
            chip.setTag(tag);
            chip.setOnCloseIconClickListener(v -> {
                Tag t = (Tag) chip.getTag();
                confirmDeleteTag(t);
            });
            chip.setOnClickListener(v -> {
                Tag t = (Tag) chip.getTag();
                showEditTagDialog(t);
            });

            chipGroupTags.addView(chip);
        }
    }

    private void showAddTagDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        EditText etTagName = dialogView.findViewById(R.id.et_tag_name);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String tagName = etTagName.getText() != null ? etTagName.getText().toString().trim() : "";
                    if (!tagName.isEmpty()) {
                        viewModel.addTag(tagName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditTagDialog(Tag tag) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        EditText etTagName = dialogView.findViewById(R.id.et_tag_name);
        etTagName.setText(tag.getName());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newTagName = etTagName.getText() != null ? etTagName.getText().toString().trim() : "";
                    if (!newTagName.isEmpty()) {
                        tag.setName(newTagName);
                        viewModel.updateTag(tag);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteTag(Tag tag) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.delete_tag_confirm, tag.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteTag(tag))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tags, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.librelibraria.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.librelibraria.R;
import com.librelibraria.data.model.Book;
import com.librelibraria.ui.activities.AddEditBookActivity;
import com.librelibraria.ui.activities.BookDetailActivity;
import com.librelibraria.ui.adapters.BookAdapter;
import com.librelibraria.ui.viewmodels.CatalogViewModel;

import java.util.ArrayList;

/**
 * Catalog fragment showing all books with search and filter.
 */
public class CatalogFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private RecyclerView rvBooks;
    private EditText etSearch;
    private View emptyView;

    private BookAdapter bookAdapter;
    private CatalogViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupListeners();
    }

    private void initViews(View view) {
        rvBooks = view.findViewById(R.id.recycler_books);
        etSearch = view.findViewById(R.id.edit_search);
        emptyView = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(new ArrayList<>(), this);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBooks.setAdapter(bookAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
    }

    private void setupObservers() {
        viewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                bookAdapter.updateBooks(books);
                emptyView.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
                rvBooks.setVisibility(books.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getGenres().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        genres
                );
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {});
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchBooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fab = getView().findViewById(R.id.fab_add_book);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddEditBookActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}

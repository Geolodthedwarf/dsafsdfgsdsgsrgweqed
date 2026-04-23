package com.librelibraria.data.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit API interface for OpenLibrary.org API.
 * Used for ISBN lookup and book metadata fetching.
 */
public interface OpenLibraryApi {

    String BASE_URL = "https://openlibrary.org/";

    /**
     * Search for books by title, author, or ISBN.
     * @param query Search query
     * @param limit Maximum number of results
     * @param fields Fields to return
     * @return Search results
     */
    @GET("search.json")
    Single<OpenLibrarySearchResponse> searchBooks(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("fields") String fields
    );

    /**
     * Get book details by ISBN.
     * @param isbn ISBN-10 or ISBN-13
     * @return Book details
     */
    @GET("api/books")
    Single<OpenLibraryBookResponse> getBookByIsbn(
            @Query("bibkeys") String isbn,
            @Query("format") String format,
            @Query("jscmd") String jscmd
    );

    /**
     * Get author details.
     * @param authorKey Author key (e.g., OL12345A)
     * @return Author details
     */
    @GET("authors/")
    Single<OpenLibraryAuthorResponse> getAuthor(@Query("author_key") String authorKey);

    // Response classes

    class OpenLibrarySearchResponse {
        @SerializedName("numFound")
        public int numFound;

        @SerializedName("start")
        public int start;

        @SerializedName("docs")
        public List<OpenLibraryDoc> docs;
    }

    class OpenLibraryDoc {
        @SerializedName("key")
        public String key;

        @SerializedName("title")
        public String title;

        @SerializedName("author_name")
        public List<String> authorName;

        @SerializedName("author_key")
        public List<String> authorKey;

        @SerializedName("isbn")
        public List<String> isbn;

        @SerializedName("publisher")
        public List<String> publisher;

        @SerializedName("publish_year")
        public List<Integer> publishYear;

        @SerializedName("first_publish_year")
        public Integer firstPublishYear;

        @SerializedName("number_of_pages_median")
        public Integer numberOfPagesMedian;

        @SerializedName("subject")
        public List<String> subject;

        @SerializedName("language")
        public List<String> language;

        @SerializedName("cover_i")
        public Integer coverId;

        @SerializedName("edition_count")
        public Integer editionCount;

        @SerializedName("cover_edition_key")
        public String coverEditionKey;

        @SerializedName("first_sentence")
        public List<String> firstSentence;
    }

    class OpenLibraryBookResponse {
        @SerializedName("title")
        public String title;

        @SerializedName("authors")
        public List<OpenLibraryAuthor> authors;

        @SerializedName("publishers")
        public List<OpenLibraryPublisher> publishers;

        @SerializedName("publish_date")
        public String publishDate;

        @SerializedName("number_of_pages")
        public Integer numberOfPages;

        @SerializedName("covers")
        public List<Integer> covers;

        @SerializedName("subjects")
        public List<OpenLibrarySubject> subjects;

        @SerializedName("description")
        public OpenLibraryDescription description;

        @SerializedName("isbn_13")
        public List<String> isbn13;

        @SerializedName("isbn_10")
        public List<String> isbn10;
    }

    class OpenLibraryAuthor {
        @SerializedName("name")
        public String name;

        @SerializedName("key")
        public String key;
    }

    class OpenLibraryPublisher {
        @SerializedName("name")
        public String name;
    }

    class OpenLibrarySubject {
        @SerializedName("name")
        public String name;

        @SerializedName("key")
        public String key;
    }

    class OpenLibraryDescription {
        @SerializedName("value")
        public String value;
    }

    class OpenLibraryAuthorResponse {
        @SerializedName("name")
        public String name;

        @SerializedName("bio")
        public String bio;

        @SerializedName("birth_date")
        public String birthDate;

        @SerializedName("death_date")
        public String deathDate;

        @SerializedName("photos")
        public List<Integer> photos;

        @SerializedName("wikipedia")
        public String wikipedia;
    }
}

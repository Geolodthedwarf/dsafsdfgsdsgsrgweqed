package com.librelibraria.data.api;

import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.Statistics;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.*;

/**
 * Retrofit API interface for PostgreSQL server communication.
 */
public interface ApiClient {

    // Book endpoints
    @GET("api/books")
    Single<List<Book>> getAllBooks();

    @GET("api/books/{id}")
    Single<Book> getBook(@Path("id") long id);

    @POST("api/books")
    Single<Book> createBook(@Body Book book);

    @PUT("api/books/{id}")
    Single<Book> updateBook(@Path("id") long id, @Body Book book);

    @DELETE("api/books/{id}")
    Single<Void> deleteBook(@Path("id") long id);

    @POST("api/books/sync")
    Single<SyncResponse> syncBooks(@Body List<Book> books);

    @GET("api/books/search")
    Single<List<Book>> searchBooks(@Query("q") String query);

    // Loan endpoints
    @GET("api/loans")
    Single<List<Loan>> getAllLoans();

    @POST("api/loans")
    Single<Loan> createLoan(@Body Loan loan);

    @PUT("api/loans/{id}")
    Single<Loan> updateLoan(@Path("id") long id, @Body Loan loan);

    @POST("api/loans/{id}/return")
    Single<Loan> returnBook(@Path("id") long id, @Body ReturnRequest request);

    @POST("api/loans/sync")
    Single<SyncResponse> syncLoans(@Body List<Loan> loans);

    // Statistics endpoint
    @GET("api/statistics")
    Single<Statistics> getStatistics();

    // Import/Export endpoints
    @POST("api/import")
    Single<ImportResponse> importLibrary(@Body String jsonData);

    @GET("api/export")
    Single<ResponseBody> exportLibrary();

    @Multipart
    @POST("api/backup")
    Single<BackupResponse> uploadBackup(@Part MultipartBody.Part file);

    // Health check
    @GET("api/health")
    Single<HealthResponse> healthCheck();

    // Request/Response classes
    class SyncResponse {
        public boolean success;
        public String message;
        public int syncedCount;
        public int conflictsCount;
        public List<Book> conflicts;
    }

    class ReturnRequest {
        public long returnDate;
        public String condition;
        public double lateFee;
    }

    class ImportResponse {
        public boolean success;
        public String message;
        public int importedCount;
        public int skippedCount;
    }

    class BackupResponse {
        public boolean success;
        public String message;
        public String backupId;
    }

    class HealthResponse {
        public boolean healthy;
        public String version;
        public long serverTime;
    }
}

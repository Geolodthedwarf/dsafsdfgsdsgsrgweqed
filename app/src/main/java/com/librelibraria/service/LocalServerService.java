package com.librelibraria.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.librelibraria.LibreLibrariaApp;
import com.librelibraria.R;
import com.librelibraria.data.database.AppDatabase;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.service.HybridLibraryService;
import com.librelibraria.ui.activities.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.iki.elonen.NanoHTTPD;

/**
 * Local HTTP server service for peer-to-peer sync.
 * Allows other LibreLibraria instances to sync with this device.
 */
public class LocalServerService extends Service {

    public static final int PORT = 8080;
    public static final String ACTION_START = "com.librelibraria.START_SERVER";
    public static final String ACTION_STOP = "com.librelibraria.STOP_SERVER";

    private static final int NOTIFICATION_ID = 1001;

    private LibreServer server;
    private ExecutorService executor;
    private AppDatabase database;
    private HybridLibraryService hybridLibraryService;

    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        hybridLibraryService = ((LibreLibrariaApp) getApplication()).getHybridLibraryService();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startServer();
            } else if (ACTION_STOP.equals(action)) {
                stopServer();
            }
        }
        return START_STICKY;
    }

    private void startServer() {
        startForeground(NOTIFICATION_ID, createNotification());

        try {
            server = new LibreServer(PORT);
            executor.execute(() -> {
                try {
                    server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, LocalServerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, LibreLibrariaApp.CHANNEL_SERVER)
                .setContentTitle(getString(R.string.server_running))
                .setContentText(getString(R.string.server_port, PORT))
                .setSmallIcon(R.drawable.ic_server)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer();
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * NanoHTTPD server implementation for LibreLibraria sync.
     */
    private class LibreServer extends NanoHTTPD {

        public LibreServer(int port) throws IOException {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            String method = session.getMethod().name();

            try {
                // API routing
                if (uri.startsWith("/api/")) {
                    return handleApiRequest(session, uri, method);
                }

                if ("/".equals(uri)) {
                    return newFixedLengthResponse(Response.Status.OK, "text/html", webUiHtml());
                }

                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response handleApiRequest(IHTTPSession session, String uri, String method) {
            if (uri.equals("/api/health") && method.equals("GET")) return healthCheck();

            // Books
            if (uri.equals("/api/books") && method.equals("GET")) return getAllBooks();
            if (uri.equals("/api/books") && method.equals("POST")) return createBook(session);
            if (uri.startsWith("/api/books/") && method.equals("PUT")) return updateBook(session, uri);
            if (uri.startsWith("/api/books/") && method.equals("DELETE")) return deleteBook(uri);

            // Borrowers
            if (uri.equals("/api/borrowers") && method.equals("GET")) return getAllBorrowers();
            if (uri.equals("/api/borrowers") && method.equals("POST")) return createBorrower(session);
            if (uri.startsWith("/api/borrowers/") && method.equals("DELETE")) return deleteBorrower(uri);

            // Loans
            if (uri.equals("/api/loans") && method.equals("GET")) return getAllLoans();
            if (uri.equals("/api/loans") && method.equals("POST")) return createLoan(session);
            if (uri.startsWith("/api/loans/") && uri.endsWith("/return") && method.equals("POST")) return returnLoan(uri);

            return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    "{\"error\":\"Endpoint not found\"}"
            );
        }

        private Response getAllBooks() {
            try {
                List<Book> books = database.bookDao().getAllBooks().firstOrError().blockingGet();
                String json = new com.google.gson.Gson().toJson(books);
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        json
                );
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response createBook(IHTTPSession session) {
            try {
                org.json.JSONObject body = readJsonBody(session);
                Book book = new Book();
                book.setTitle(body.optString("title", ""));
                book.setAuthor(body.optString("author", ""));
                book.setIsbn(body.optString("isbn", ""));
                book.setPublisher(body.optString("publisher", ""));
                book.setPublishYear(body.optString("publishYear", ""));
                book.setGenre(body.optString("genre", ""));
                book.setLanguage(body.optString("language", "Ukrainian"));
                book.setDescription(body.optString("description", ""));
                book.setShelfLocation(body.optString("shelfLocation", ""));
                book.setCustomCoverUrl(body.optString("customCoverUrl", ""));
                book.setCopies(body.optInt("copies", 1));
                book.setAvailableCopies(body.optInt("availableCopies", Math.max(1, book.getCopies())));

                long id = hybridLibraryService.saveBook(book).blockingGet();
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        "{\"id\":" + id + "}"
                );
            } catch (Exception e) {
                return newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }
        }

        private Response updateBook(IHTTPSession session, String uri) {
            try {
                long id = Long.parseLong(uri.substring("/api/books/".length()));
                Book existing = database.bookDao().getById(id).blockingGet();
                if (existing == null) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Book not found\"}");
                }

                org.json.JSONObject body = readJsonBody(session);
                if (body.has("title")) existing.setTitle(body.optString("title", existing.getTitle()));
                if (body.has("author")) existing.setAuthor(body.optString("author", existing.getAuthor()));
                if (body.has("isbn")) existing.setIsbn(body.optString("isbn", existing.getIsbn()));
                if (body.has("publisher")) existing.setPublisher(body.optString("publisher", existing.getPublisher()));
                if (body.has("publishYear")) existing.setPublishYear(body.optString("publishYear", existing.getPublishYear()));
                if (body.has("genre")) existing.setGenre(body.optString("genre", existing.getGenre()));
                if (body.has("language")) existing.setLanguage(body.optString("language", existing.getLanguage()));
                if (body.has("description")) existing.setDescription(body.optString("description", existing.getDescription()));
                if (body.has("shelfLocation")) existing.setShelfLocation(body.optString("shelfLocation", existing.getShelfLocation()));
                if (body.has("customCoverUrl")) existing.setCustomCoverUrl(body.optString("customCoverUrl", existing.getCustomCoverUrl()));
                if (body.has("copies")) existing.setCopies(body.optInt("copies", existing.getCopies()));
                if (body.has("availableCopies")) existing.setAvailableCopies(body.optInt("availableCopies", existing.getAvailableCopies()));

                hybridLibraryService.saveBook(existing).blockingGet();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response deleteBook(String uri) {
            try {
                long id = Long.parseLong(uri.substring("/api/books/".length()));
                hybridLibraryService.deleteBook(id).blockingAwait();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response getAllBorrowers() {
            try {
                List<Borrower> borrowers = database.borrowerDao().getAllBorrowers().firstOrError().blockingGet();
                String json = new com.google.gson.Gson().toJson(borrowers);
                return newFixedLengthResponse(Response.Status.OK, "application/json", json);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response createBorrower(IHTTPSession session) {
            try {
                org.json.JSONObject body = readJsonBody(session);
                Borrower borrower = new Borrower();
                borrower.setName(body.optString("name", ""));
                borrower.setEmail(body.optString("email", ""));
                borrower.setPhone(body.optString("phone", ""));
                borrower.setNotes(body.optString("notes", ""));
                long id = hybridLibraryService.saveBorrower(borrower).blockingGet();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"id\":" + id + "}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response deleteBorrower(String uri) {
            try {
                long id = Long.parseLong(uri.substring("/api/borrowers/".length()));
                hybridLibraryService.deleteBorrower(id).blockingAwait();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response getAllLoans() {
            try {
                List<Loan> loans = database.loanDao().getAllLoans().firstOrError().blockingGet();
                String json = new com.google.gson.Gson().toJson(loans);
                return newFixedLengthResponse(Response.Status.OK, "application/json", json);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response createLoan(IHTTPSession session) {
            try {
                org.json.JSONObject body = readJsonBody(session);
                long bookId = body.optLong("bookId", -1);
                long borrowerId = body.optLong("borrowerId", -1);
                long dueDate = body.optLong("dueDate", 0);

                Borrower borrower = database.borrowerDao().getById(borrowerId).blockingGet();
                if (borrower == null) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Borrower not found\"}");
                }

                long id = hybridLibraryService.lendBook(bookId, borrowerId, borrower.getName(), dueDate).blockingGet();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"id\":" + id + "}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response returnLoan(String uri) {
            try {
                String loanIdStr = uri.substring("/api/loans/".length(), uri.length() - "/return".length());
                long loanId = Long.parseLong(loanIdStr);
                hybridLibraryService.returnBook(loanId).blockingAwait();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private Response healthCheck() {
            return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    "{\"healthy\":true,\"serverTime\":" + System.currentTimeMillis() + "}"
            );
        }

        private org.json.JSONObject readJsonBody(IHTTPSession session) throws Exception {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            String postData = files.get("postData");
            if (postData == null) postData = "{}";
            return new org.json.JSONObject(postData);
        }

        private String webUiHtml() {
            return "<!doctype html><html><head><meta charset='utf-8'/>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1'/>"
                    + "<title>LibreLibraria</title>"
                    + "<style>body{font-family:system-ui,Arial;margin:16px;max-width:900px}"
                    + "h1{margin:0 0 12px} .row{display:flex;gap:8px;flex-wrap:wrap}"
                    + "input,button{padding:8px;font-size:14px} table{width:100%;border-collapse:collapse;margin-top:12px}"
                    + "td,th{border-bottom:1px solid #ddd;padding:8px;text-align:left} code{background:#f4f4f4;padding:2px 4px;border-radius:4px}"
                    + "</style></head><body>"
                    + "<h1>LibreLibraria Web UI</h1>"
                    + "<p>API: <code>/api/books</code>, <code>/api/borrowers</code>, <code>/api/loans</code></p>"
                    + "<div class='row'>"
                    + "<button onclick='loadAll()'>Refresh</button>"
                    + "</div>"
                    + "<h2>Books</h2>"
                    + "<div class='row'>"
                    + "<input id='book_title' placeholder='Title'/>"
                    + "<input id='book_author' placeholder='Author'/>"
                    + "<input id='book_isbn' placeholder='ISBN'/>"
                    + "<button onclick='addBook()'>Add</button>"
                    + "</div>"
                    + "<div id='books'></div>"
                    + "<h2>Borrowers</h2>"
                    + "<div class='row'>"
                    + "<input id='bor_name' placeholder='Name'/>"
                    + "<input id='bor_email' placeholder='Email'/>"
                    + "<button onclick='addBorrower()'>Add</button>"
                    + "</div>"
                    + "<div id='borrowers'></div>"
                    + "<h2>Loans</h2>"
                    + "<div class='row'>"
                    + "<input id='loan_bookId' placeholder='Book ID'/>"
                    + "<input id='loan_borrowerId' placeholder='Borrower ID'/>"
                    + "<button onclick='lend()'>Lend</button>"
                    + "</div>"
                    + "<div id='loans'></div>"
                    + "<script>"
                    + "async function jget(u){return (await fetch(u)).json();}"
                    + "async function jpost(u,b){return (await fetch(u,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(b)})).json();}"
                    + "async function jdel(u){return (await fetch(u,{method:'DELETE'})).json();}"
                    + "async function loadAll(){"
                    + "const books=await jget('/api/books');"
                    + "document.getElementById('books').innerHTML=renderBooks(books);"
                    + "const borrowers=await jget('/api/borrowers');"
                    + "document.getElementById('borrowers').innerHTML=renderBorrowers(borrowers);"
                    + "const loans=await jget('/api/loans');"
                    + "document.getElementById('loans').innerHTML=renderLoans(loans);"
                    + "}"
                    + "function renderBooks(list){"
                    + "let h='<table><tr><th>ID</th><th>Title</th><th>Author</th><th></th></tr>';"
                    + "for(const b of list){h+=`<tr><td>${b.id}</td><td>${b.title||''}</td><td>${b.author||''}</td><td><button onclick=\"delBook(${b.id})\">Delete</button></td></tr>`}"
                    + "return h+'</table>'}"
                    + "function renderBorrowers(list){"
                    + "let h='<table><tr><th>ID</th><th>Name</th><th>Email</th><th></th></tr>';"
                    + "for(const b of list){h+=`<tr><td>${b.id}</td><td>${b.name||''}</td><td>${b.email||''}</td><td><button onclick=\"delBorrower(${b.id})\">Delete</button></td></tr>`}"
                    + "return h+'</table>'}"
                    + "function renderLoans(list){"
                    + "let h='<table><tr><th>ID</th><th>Book</th><th>Borrower</th><th>Status</th><th></th></tr>';"
                    + "for(const l of list){"
                    + "const ret=(l.status==='ACTIVE')?`<button onclick=\"returnLoan(${l.id})\">Return</button>`:'';"
                    + "h+=`<tr><td>${l.id}</td><td>${l.bookId}</td><td>${l.borrowerId||''}</td><td>${l.status}</td><td>${ret}</td></tr>`}"
                    + "return h+'</table>'}"
                    + "async function addBook(){await jpost('/api/books',{title:book_title.value,author:book_author.value,isbn:book_isbn.value});loadAll();}"
                    + "async function delBook(id){await jdel('/api/books/'+id);loadAll();}"
                    + "async function addBorrower(){await jpost('/api/borrowers',{name:bor_name.value,email:bor_email.value});loadAll();}"
                    + "async function delBorrower(id){await jdel('/api/borrowers/'+id);loadAll();}"
                    + "async function lend(){await jpost('/api/loans',{bookId:Number(loan_bookId.value),borrowerId:Number(loan_borrowerId.value)});loadAll();}"
                    + "async function returnLoan(id){await jpost('/api/loans/'+id+'/return',{});loadAll();}"
                    + "loadAll();"
                    + "</script>"
                    + "</body></html>";
        }
    }
}

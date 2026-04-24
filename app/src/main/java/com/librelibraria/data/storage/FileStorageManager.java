package com.librelibraria.data.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.documentfile.provider.DocumentFile;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.model.ReadingStatus;
import com.librelibraria.data.model.Tag;
import com.librelibraria.data.model.AppTheme;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileStorageManager {
    private static final String BASE_FOLDER = "LibreLibraria";
    private static final String BOOKS_FOLDER = "books";
    private static final String LOANS_FOLDER = "loans";
    private static final String BORROWERS_FOLDER = "borrowers";
    private static final String TAGS_FOLDER = "tags";
    private static final String THEMES_FOLDER = "themes";
    private static final String LIBRARY_FOLDER = "library";

    public enum ImportMode {
        OVERWRITE,
        SKIP,
        CREATE_NEW
    }

    public boolean deleteBorrowerById(long borrowerId) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                Uri uri = Uri.parse(baseUri);
                DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
                if (baseDir == null) return false;

                DocumentFile dir = baseDir.findFile(BORROWERS_FOLDER);
                if (dir == null || !dir.isDirectory()) return false;

                DocumentFile match = findJsonDocFileById(dir, ".bcbr", borrowerId);
                if (match != null) match.delete();
                return true;
            }

            File dir = new File(getBasePath(), BORROWERS_FOLDER);
            File match = findJsonFileById(dir, ".bcbr", borrowerId);
            if (match != null) match.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteLoansForBook(long bookId) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                DocumentFile baseDir = DocumentFile.fromTreeUri(context, Uri.parse(baseUri));
                if (baseDir == null) return false;

                DocumentFile loansDir = baseDir.findFile(LOANS_FOLDER);
                if (loansDir == null || !loansDir.isDirectory()) return false;

                for (DocumentFile f : loansDir.listFiles()) {
                    String name = f.getName();
                    if (name == null || !name.endsWith(".bclo")) continue;
                    try (InputStream in = context.getContentResolver().openInputStream(f.getUri())) {
                        if (in == null) continue;
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) sb.append(line);
                        JSONObject obj = new JSONObject(sb.toString());
                        if (obj.optLong("bookId", -1) == bookId) {
                            f.delete();
                        }
                    } catch (Exception ignored) {
                    }
                }
                return true;
            }

            File dir = new File(getBasePath(), LOANS_FOLDER);
            File[] files = dir.listFiles((d, name) -> name != null && name.endsWith(".bclo"));
            if (files == null) return true;
            for (File f : files) {
                try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    JSONObject obj = new JSONObject(sb.toString());
                    if (obj.optLong("bookId", -1) == bookId) {
                        f.delete();
                    }
                } catch (Exception ignored) {
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static FileStorageManager instance;
    private Context context;
    private String basePath;
    private final SharedPreferences prefs;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    
    private FileStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE);
    }
    
    public static synchronized FileStorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new FileStorageManager(context);
        }
        return instance;
    }
    
    public boolean isFolderSelected() {
        return prefs.getString("base_uri", null) != null || prefs.getString("base_path", null) != null;
    }

    /**
     * Validates that the stored SAF URI is still accessible.
     * Returns true if storage is accessible, false if we need to fall back.
     */
    public boolean validateStorageAccess() {
        String baseUri = prefs.getString("base_uri", null);
        String basePath = getBasePath();

        if (baseUri != null) {
            try {
                Uri uri = Uri.parse(baseUri);
                DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
                if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
                    return true;
                }
            } catch (Exception e) {
                android.util.Log.w("FileStorageManager", "SAF storage validation failed: " + e.getMessage());
            }
            // SAF URI is invalid - clear it and fall back to file path
            android.util.Log.w("FileStorageManager", "SAF URI is no longer accessible, clearing and using file path");
            clearBaseUri();
            // Try to ensure we have a valid file path
            if (basePath == null || basePath.isEmpty()) {
                // Set default path
                File defaultDir = context.getFilesDir();
                if (defaultDir != null) {
                    setBasePath(new File(defaultDir, BASE_FOLDER).getAbsolutePath());
                }
            }
            return false;
        }

        // Check if file path is accessible
        if (basePath != null && !basePath.isEmpty()) {
            File dir = new File(basePath);
            if (dir.exists() && dir.isDirectory()) {
                return true;
            }
            // Path doesn't exist - try to create it
            return createBaseFolders();
        }

        // No valid storage - return false
        return false;
    }

    /**
     * Clear the stored SAF URI.
     */
    public void clearBaseUri() {
        prefs.edit().remove("base_uri").apply();
    }
    
    public void setBaseUri(String uri) {
        prefs.edit().putString("base_uri", uri).apply();
    }
    
    public String getBasePath() {
        if (basePath == null) {
            basePath = prefs.getString("base_path", null);
        }
        return basePath;
    }
    
    public void setBasePath(String path) {
        basePath = path;
        prefs.edit().putString("base_path", path).apply();
    }
    
    public boolean createLibreLibrariaFolder(Uri treeUri) {
        try {
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, treeUri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile libreFolder = baseDir.findFile(BASE_FOLDER);
            if (libreFolder == null || !libreFolder.isDirectory()) {
                libreFolder = baseDir.createDirectory(BASE_FOLDER);
            }
            
            if (libreFolder != null && libreFolder.exists()) {
                // Store the LibreLibraria folder itself as the base URI.
                setBaseUri(libreFolder.getUri().toString());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initialize storage using a user-selected SAF tree Uri.
     * Creates a LibreLibraria folder + required subfolders, and persists the base URI.
     */
    public boolean initializeFromSelectedTree(Uri treeUri) {
        try {
            if (treeUri == null) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: treeUri is null");
                return false;
            }

            // Validate the URI scheme
            String scheme = treeUri.getScheme();
            if (scheme == null || !"content".equals(scheme)) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: Invalid URI scheme: " + scheme);
                return false;
            }

            DocumentFile picked = DocumentFile.fromTreeUri(context, treeUri);
            if (picked == null) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: DocumentFile is null for URI");
                return false;
            }

            if (!picked.exists()) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: Selected directory does not exist");
                return false;
            }

            if (!picked.isDirectory()) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: Selected path is not a directory");
                return false;
            }

            // Check if LibreLibraria folder exists, create if not
            DocumentFile libre = picked.findFile(BASE_FOLDER);
            if (libre == null || !libre.exists() || !libre.isDirectory()) {
                // Try to create the LibreLibraria folder
                try {
                    libre = picked.createDirectory(BASE_FOLDER);
                } catch (Exception e) {
                    android.util.Log.e("FileStorageManager", "Failed to create LibreLibraria folder: " + e.getMessage());
                }
            }

            if (libre == null || !libre.exists() || !libre.isDirectory()) {
                android.util.Log.e("FileStorageManager", "initializeFromSelectedTree: Could not create/verify LibreLibraria folder");
                return false;
            }

            // Persist base URI as the LibreLibraria folder uri.
            setBaseUri(libre.getUri().toString());
            // Best-effort: store a human-friendly "path" label (SAF doesn't provide real filesystem paths).
            setBasePath(BASE_FOLDER);

            // Create required subfolders
            String[] folders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER, LIBRARY_FOLDER};
            for (String folderName : folders) {
                try {
                    DocumentFile folder = libre.findFile(folderName);
                    if (folder == null || !folder.exists() || !folder.isDirectory()) {
                        try {
                            folder = libre.createDirectory(folderName);
                        } catch (Exception e) {
                            android.util.Log.w("FileStorageManager", "Could not create folder " + folderName + ": " + e.getMessage());
                        }
                    }
                    if (folder == null || !folder.exists() || !folder.isDirectory()) {
                        android.util.Log.w("FileStorageManager", "Folder " + folderName + " not available, will be created on demand");
                    }
                } catch (Exception e) {
                    android.util.Log.w("FileStorageManager", "Error checking folder " + folderName + ": " + e.getMessage());
                    // Continue with other folders - don't fail completely
                }
            }

            android.util.Log.i("FileStorageManager", "Successfully initialized SAF storage at: " + libre.getUri());
            return true;
        } catch (Exception e) {
            android.util.Log.e("FileStorageManager", "initializeFromSelectedTree exception: " + e.getMessage(), e);
            return false;
        }
    }
    
    public boolean createBaseFolders() {
        String basePath = getBasePath();
        if (basePath == null || basePath.isEmpty()) {
            android.util.Log.w("FileStorageManager", "createBaseFolders: basePath is null or empty");
            return false;
        }

        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return createFoldersViaDocumentFile(baseUri);
            }

            File baseDir = new File(basePath);

            // Ensure base directory exists
            if (!baseDir.exists()) {
                boolean created = baseDir.mkdirs();
                if (!created && !baseDir.exists()) {
                    android.util.Log.e("FileStorageManager", "Could not create base directory: " + basePath);
                    return false;
                }
            }

            // Create subfolders
            String[] subFolders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER, LIBRARY_FOLDER};
            boolean allCreated = true;

            for (String folderName : subFolders) {
                File subDir = new File(baseDir, folderName);
                if (!subDir.exists()) {
                    boolean created = subDir.mkdirs();
                    if (!created && !subDir.exists()) {
                        android.util.Log.w("FileStorageManager", "Could not create folder: " + folderName);
                        allCreated = false;
                    }
                }
            }

            // Return true if at least the base directory exists
            return baseDir.exists() && baseDir.isDirectory();
        } catch (Exception e) {
            android.util.Log.e("FileStorageManager", "createBaseFolders exception: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean ensureDirectory(File dir) {
        if (dir.exists()) return true;
        boolean created = dir.mkdirs();
        if (!created) {
            created = dir.mkdir();
        }
        return dir.exists();
    }
    
    private boolean createFoldersViaDocumentFile(String uriStr) {
        try {
            if (uriStr == null || uriStr.isEmpty()) {
                android.util.Log.w("FileStorageManager", "createFoldersViaDocumentFile: uriStr is null or empty");
                return false;
            }

            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);

            if (baseDir == null) {
                android.util.Log.e("FileStorageManager", "createFoldersViaDocumentFile: DocumentFile is null");
                return false;
            }

            if (!baseDir.exists()) {
                android.util.Log.e("FileStorageManager", "createFoldersViaDocumentFile: Directory does not exist");
                return false;
            }

            if (!baseDir.isDirectory()) {
                android.util.Log.e("FileStorageManager", "createFoldersViaDocumentFile: Not a directory");
                return false;
            }

            String[] folders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER, LIBRARY_FOLDER};
            boolean allCreated = true;

            for (String folderName : folders) {
                try {
                    DocumentFile folder = baseDir.findFile(folderName);
                    if (folder == null || !folder.exists() || !folder.isDirectory()) {
                        try {
                            folder = baseDir.createDirectory(folderName);
                        } catch (Exception e) {
                            android.util.Log.w("FileStorageManager", "Could not create folder " + folderName + ": " + e.getMessage());
                        }
                    }
                    if (folder == null || !folder.exists() || !folder.isDirectory()) {
                        android.util.Log.w("FileStorageManager", "Folder " + folderName + " not available");
                        allCreated = false;
                    }
                } catch (Exception e) {
                    android.util.Log.w("FileStorageManager", "Error creating folder " + folderName + ": " + e.getMessage());
                    allCreated = false;
                }
            }

            return baseDir.exists() && baseDir.isDirectory();
        } catch (Exception e) {
            android.util.Log.e("FileStorageManager", "createFoldersViaDocumentFile exception: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean createFolder(String name) {
        File folder = new File(getBasePath(), name);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }
    
    private boolean checkAllFoldersExist() {
        File baseDir = new File(getBasePath());
        return new File(baseDir, BOOKS_FOLDER).exists() &&
               new File(baseDir, LOANS_FOLDER).exists() &&
               new File(baseDir, BORROWERS_FOLDER).exists() &&
               new File(baseDir, TAGS_FOLDER).exists() &&
               new File(baseDir, THEMES_FOLDER).exists() &&
               new File(baseDir, LIBRARY_FOLDER).exists();
    }
    
    public String generateFileName(String type, String extension) {
        String dateTime = DATE_FORMAT.format(new Date());
        String random = String.format("%04X", new Random().nextInt(65536));
        return type + "_" + dateTime + "_" + random + "." + extension;
    }

    private long ensureId(long id) {
        return id > 0 ? id : System.currentTimeMillis();
    }

    private File findJsonFileById(File dir, String extension, long id) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return null;
        File[] files = dir.listFiles((d, name) -> name != null && name.endsWith(extension));
        if (files == null) return null;

        for (File f : files) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                JSONObject obj = new JSONObject(sb.toString());
                if (obj.optLong("id", -1) == id) return f;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private DocumentFile findJsonDocFileById(DocumentFile dir, String extension, long id) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return null;
        for (DocumentFile f : dir.listFiles()) {
            try {
                String name = f.getName();
                if (name == null || !name.endsWith(extension)) continue;
                try (InputStream in = context.getContentResolver().openInputStream(f.getUri())) {
                    if (in == null) continue;
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    JSONObject obj = new JSONObject(sb.toString());
                    if (obj.optLong("id", -1) == id) return f;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
    
    // Loan operations
    public boolean saveBook(Book book) {
        try {
            book.setId(ensureId(book.getId()));
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveBookViaDocumentFile(book, baseUri);
            }
            
            JSONObject json = bookToJson(book);
            File booksDir = new File(getBasePath(), BOOKS_FOLDER);
            File file = findJsonFileById(booksDir, ".bcb", book.getId());
            if (file == null) {
                String fileName = generateFileName("book", "bcb");
                file = new File(booksDir, fileName);
            }
            
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean saveBookViaDocumentFile(Book book, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile booksDir = baseDir.findFile(BOOKS_FOLDER);
            if (booksDir == null || !booksDir.isDirectory()) {
                booksDir = baseDir.createDirectory(BOOKS_FOLDER);
            }
            if (booksDir == null) return false;
            
            JSONObject json = bookToJson(book);
            DocumentFile bookFile = findJsonDocFileById(booksDir, ".bcb", book.getId());
            if (bookFile == null) {
                String fileName = generateFileName("book", "bcb");
                bookFile = booksDir.createFile("application/octet-stream", fileName);
            }
            if (bookFile == null) return false;
            
            java.io.OutputStream out = context.getContentResolver().openOutputStream(bookFile.getUri());
            if (out != null) {
                out.write(json.toString(2).getBytes());
                out.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Book> loadAllBooks() {
        List<Book> books = new ArrayList<>();
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return loadAllBooksViaDocumentFile(baseUri);
            }
            
            File booksDir = new File(getBasePath(), BOOKS_FOLDER);
            if (booksDir.exists()) {
                File[] files = booksDir.listFiles((dir, name) -> name.endsWith(".bcb"));
                if (files != null) {
                    for (File file : files) {
                        Book book = loadBook(file);
                        if (book != null) books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }
    
    private List<Book> loadAllBooksViaDocumentFile(String uriStr) {
        List<Book> books = new ArrayList<>();
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null) return books;
            
            DocumentFile booksDir = baseDir.findFile(BOOKS_FOLDER);
            if (booksDir == null || !booksDir.isDirectory()) return books;
            
            for (DocumentFile file : booksDir.listFiles()) {
                if (file.getName() != null && file.getName().endsWith(".bcb")) {
                    try {
                        java.io.InputStream in = context.getContentResolver().openInputStream(file.getUri());
                        if (in != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder json = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                json.append(line);
                            }
                            reader.close();
                            in.close();
                            books.add(jsonToBook(new JSONObject(json.toString())));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }
    
    public Book loadBook(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            return jsonToBook(new JSONObject(json.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean deleteBook(String bookId) {
        try {
            long id = -1;
            try { id = Long.parseLong(bookId); } catch (Exception ignored) {}

            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return deleteBookViaDocumentFile(bookId, baseUri);
            }
            
            File booksDir = new File(getBasePath(), BOOKS_FOLDER);
            if (id > 0) {
                File match = findJsonFileById(booksDir, ".bcb", id);
                if (match != null) match.delete();
            } else {
                File[] files = booksDir.listFiles((dir, name) -> name != null && name.contains(bookId));
                if (files != null) {
                    for (File f : files) f.delete();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean deleteBookViaDocumentFile(String bookId, String uriStr) {
        try {
            long id = -1;
            try { id = Long.parseLong(bookId); } catch (Exception ignored) {}

            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null) return false;
            
            DocumentFile booksDir = baseDir.findFile(BOOKS_FOLDER);
            if (booksDir == null || !booksDir.isDirectory()) return false;

            if (id > 0) {
                DocumentFile match = findJsonDocFileById(booksDir, ".bcb", id);
                if (match != null) match.delete();
            } else {
                for (DocumentFile file : booksDir.listFiles()) {
                    if (file.getName() != null && file.getName().contains(bookId)) file.delete();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Borrower operations
    public boolean saveBorrower(Borrower borrower) {
        try {
            borrower.setId(ensureId(borrower.getId()));
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveBorrowerViaDocumentFile(borrower, baseUri);
            }
            
            JSONObject json = borrowerToJson(borrower);
            File dir = new File(getBasePath(), BORROWERS_FOLDER);
            File file = findJsonFileById(dir, ".bcbr", borrower.getId());
            if (file == null) file = new File(dir, generateFileName("borrower", "bcbr"));
            
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean saveBorrowerViaDocumentFile(Borrower borrower, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile dir = baseDir.findFile(BORROWERS_FOLDER);
            if (dir == null || !dir.isDirectory()) {
                dir = baseDir.createDirectory(BORROWERS_FOLDER);
            }
            if (dir == null) return false;
            
            JSONObject json = borrowerToJson(borrower);
            DocumentFile file = findJsonDocFileById(dir, ".bcbr", borrower.getId());
            if (file == null) {
                file = dir.createFile("application/octet-stream", generateFileName("borrower", "bcbr"));
            }
            if (file == null) return false;
            
            java.io.OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            if (out != null) {
                out.write(json.toString(2).getBytes());
                out.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean saveLoanViaDocumentFile(Loan loan, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile dir = baseDir.findFile(LOANS_FOLDER);
            if (dir == null || !dir.isDirectory()) {
                dir = baseDir.createDirectory(LOANS_FOLDER);
            }
            if (dir == null) return false;
            
            JSONObject json = loanToJson(loan);
            loan.setId(ensureId(loan.getId()));
            DocumentFile file = findJsonDocFileById(dir, ".bclo", loan.getId());
            if (file == null) {
                file = dir.createFile("application/octet-stream", generateFileName("loan", "bclo"));
            }
            if (file == null) return false;
            
            java.io.OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            if (out != null) {
                out.write(json.toString(2).getBytes());
                out.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Borrower> loadAllBorrowers() {
        List<Borrower> borrowers = new ArrayList<>();
        try {
            File dir = new File(getBasePath(), BORROWERS_FOLDER);
            if (dir.exists()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".bcbr"));
                if (files != null) {
                    for (File file : files) {
                        Borrower b = loadBorrower(file);
                        if (b != null) borrowers.add(b);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return borrowers;
    }
    
    public Borrower loadBorrower(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);
            reader.close();
            return jsonToBorrower(new JSONObject(json.toString()));
        } catch (Exception e) { return null; }
    }
    
    // Loan operations  
    public boolean saveLoan(Loan loan) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveLoanViaDocumentFile(loan, baseUri);
            }
            
            loan.setId(ensureId(loan.getId()));
            JSONObject json = loanToJson(loan);
            File dir = new File(getBasePath(), LOANS_FOLDER);
            File file = findJsonFileById(dir, ".bclo", loan.getId());
            if (file == null) file = new File(dir, generateFileName("loan", "bclo"));
            
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) { return false; }
    }
    
    public List<Loan> loadAllLoans() {
        List<Loan> loans = new ArrayList<>();
        try {
            File dir = new File(getBasePath(), LOANS_FOLDER);
            if (dir.exists()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".bclo"));
                if (files != null) {
                    for (File file : files) {
                        Loan loan = loadLoan(file);
                        if (loan != null) loans.add(loan);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return loans;
    }
    
    public Loan loadLoan(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);
            reader.close();
            return jsonToLoan(new JSONObject(json.toString()));
        } catch (Exception e) { return null; }
    }
    
    // Tag operations
    public boolean saveTag(Tag tag) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveTagViaDocumentFile(tag, baseUri);
            }
            
            tag.setId(ensureId(tag.getId()));
            JSONObject json = tagToJson(tag);
            File dir = new File(getBasePath(), TAGS_FOLDER);
            File file = findJsonFileById(dir, ".bct", tag.getId());
            if (file == null) file = new File(dir, generateFileName("tag", "bct"));
            
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean saveTagViaDocumentFile(Tag tag, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile dir = baseDir.findFile(TAGS_FOLDER);
            if (dir == null || !dir.isDirectory()) {
                dir = baseDir.createDirectory(TAGS_FOLDER);
            }
            if (dir == null) return false;
            
            tag.setId(ensureId(tag.getId()));
            JSONObject json = tagToJson(tag);
            DocumentFile file = findJsonDocFileById(dir, ".bct", tag.getId());
            if (file == null) {
                file = dir.createFile("application/octet-stream", generateFileName("tag", "bct"));
            }
            if (file == null) return false;
            
            java.io.OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            if (out != null) {
                out.write(json.toString(2).getBytes());
                out.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    public List<Tag> loadAllTags() {
        List<Tag> tags = new ArrayList<>();
        try {
            File dir = new File(getBasePath(), TAGS_FOLDER);
            if (dir.exists()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".bct"));
                if (files != null) {
                    for (File file : files) {
                        Tag tag = loadTag(file);
                        if (tag != null) tags.add(tag);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tags;
    }
    
    public Tag loadTag(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);
            reader.close();
            return jsonToTag(new JSONObject(json.toString()));
        } catch (Exception e) { return null; }
    }
    
    // BCL Export (ZIP archive)
    public boolean exportToBCL(String fileName) {
        try {
            String timestamp = DATE_FORMAT.format(new Date()); // yyyyMMdd_HHmmss
            String fullName = "LibreLibraria_Export_" + timestamp + ".bcl";

            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                // SAF mode: write into /LibreLibraria/library/
                DocumentFile baseDir = DocumentFile.fromTreeUri(context, Uri.parse(baseUri));
                if (baseDir == null || !baseDir.exists() || !baseDir.isDirectory()) return false;

                DocumentFile libraryDir = baseDir.findFile(LIBRARY_FOLDER);
                if (libraryDir == null || !libraryDir.isDirectory()) {
                    libraryDir = baseDir.createDirectory(LIBRARY_FOLDER);
                }
                if (libraryDir == null || !libraryDir.exists() || !libraryDir.isDirectory()) return false;

                DocumentFile zipDoc = libraryDir.createFile("application/octet-stream", fullName);
                if (zipDoc == null) return false;

                try (OutputStream out = context.getContentResolver().openOutputStream(zipDoc.getUri())) {
                    if (out == null) return false;
                    exportToBCL(out);
                }
                return true;
            }

            // File API mode: write into /LibreLibraria/library/
            File baseDir = new File(getBasePath());
            File exportDir = new File(baseDir, LIBRARY_FOLDER);
            if (!exportDir.exists()) exportDir.mkdirs();

            File zipFile = new File(exportDir, fullName);
            try (OutputStream out = new FileOutputStream(zipFile)) {
                exportToBCL(out);
            }
            return zipFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Write a BCL (zip) of the entire LibreLibraria folder to the provided stream.
     * Uses SAF traversal when base_uri is set; otherwise uses File API traversal.
     */
    public void exportToBCL(OutputStream out) throws IOException {
        String baseUri = prefs.getString("base_uri", null);
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            if (baseUri != null) {
                DocumentFile baseDir = DocumentFile.fromTreeUri(context, Uri.parse(baseUri));
                if (baseDir != null) {
                    addDocumentFolderToZip(zos, baseDir, "");
                }
            } else {
                File baseDir = new File(getBasePath());
                addFolderToZip(zos, baseDir, baseDir);
            }
        }
    }

    private void addDocumentFolderToZip(ZipOutputStream zos, DocumentFile node, String relativePath) throws IOException {
        if (node == null) return;

        if (node.isDirectory()) {
            String nextBase = relativePath;
            if (node.getName() != null && !node.getName().isEmpty()) {
                if (!relativePath.isEmpty()) nextBase = relativePath + "/" + node.getName();
                else nextBase = node.getName();
            }
            for (DocumentFile child : node.listFiles()) {
                addDocumentFolderToZip(zos, child, nextBase);
            }
            return;
        }

        String name = node.getName();
        if (name == null || name.isEmpty()) return;

        String entryName = relativePath.isEmpty() ? name : (relativePath + "/" + name);
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (InputStream in = context.getContentResolver().openInputStream(node.getUri())) {
            if (in != null) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
        }
        zos.closeEntry();
    }
    
    private void addFolderToZip(ZipOutputStream zos, File sourceFile, File baseDir) throws IOException {
        if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFolderToZip(zos, file, baseDir);
                }
            }
        } else {
            String entryName = sourceFile.getPath().substring(baseDir.getPath().length() + 1);
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            
            FileInputStream fis = new FileInputStream(sourceFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            fis.close();
            zos.closeEntry();
        }
    }

    public int importFromBcl(InputStream bclInputStream, ImportMode mode) throws IOException {
        if (bclInputStream == null) throw new IllegalArgumentException("Input stream is null");
        if (mode == null) mode = ImportMode.SKIP;

        String baseUri = prefs.getString("base_uri", null);
        if (baseUri != null) {
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, Uri.parse(baseUri));
            if (baseDir == null || !baseDir.exists() || !baseDir.isDirectory()) return 0;
            if (mode == ImportMode.OVERWRITE) {
                clearSubfoldersSaf(baseDir);
            }
            return importZipToSaf(bclInputStream, baseDir, mode);
        }

        File baseDir = new File(getBasePath());
        if (mode == ImportMode.OVERWRITE) {
            clearSubfoldersFile(baseDir);
        }
        return importZipToFile(bclInputStream, baseDir, mode);
    }

    private void clearSubfoldersFile(File baseDir) {
        if (baseDir == null || !baseDir.exists()) return;
        String[] folders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER};
        for (String folder : folders) {
            File dir = new File(baseDir, folder);
            File[] files = dir.listFiles();
            if (files == null) continue;
            for (File f : files) {
                // keep the library folder intact
                if (f.isFile()) f.delete();
            }
        }
    }

    private void clearSubfoldersSaf(DocumentFile baseDir) {
        String[] folders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER};
        for (String folder : folders) {
            DocumentFile dir = baseDir.findFile(folder);
            if (dir == null || !dir.isDirectory()) continue;
            for (DocumentFile f : dir.listFiles()) {
                if (f.isFile()) f.delete();
            }
        }
    }

    private int importZipToFile(InputStream in, File baseDir, ImportMode mode) throws IOException {
        int imported = 0;
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String entryName = normalizeZipEntryName(entry.getName());
                if (entryName == null) continue;
                if (!isSupportedImportEntry(entryName)) continue;

                File outFile = new File(baseDir, entryName);
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                if (mode == ImportMode.SKIP && outFile.exists()) continue;

                if (mode == ImportMode.CREATE_NEW && outFile.exists()) {
                    outFile = new File(parent, generateImportFileName(entryName));
                }

                byte[] bytes = readAllBytes(zis);
                if (mode == ImportMode.CREATE_NEW && looksLikeJson(entryName)) {
                    bytes = rewriteJsonId(bytes);
                }

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(bytes);
                }
                imported++;
            }
        }
        return imported;
    }

    private int importZipToSaf(InputStream in, DocumentFile baseDir, ImportMode mode) throws IOException {
        int imported = 0;
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String entryName = normalizeZipEntryName(entry.getName());
                if (entryName == null) continue;
                if (!isSupportedImportEntry(entryName)) continue;

                DocumentFile parentDir = ensureSafParentDir(baseDir, entryName);
                if (parentDir == null) continue;

                String leafName = new File(entryName).getName();
                DocumentFile existing = parentDir.findFile(leafName);
                if (mode == ImportMode.SKIP && existing != null && existing.exists()) continue;

                String targetName = leafName;
                if (mode == ImportMode.CREATE_NEW && existing != null && existing.exists()) {
                    targetName = generateImportFileName(entryName);
                }

                byte[] bytes = readAllBytes(zis);
                if (mode == ImportMode.CREATE_NEW && looksLikeJson(entryName)) {
                    bytes = rewriteJsonId(bytes);
                }

                DocumentFile outDoc = parentDir.findFile(targetName);
                if (outDoc == null) {
                    outDoc = parentDir.createFile("application/octet-stream", targetName);
                }
                if (outDoc == null) continue;

                try (OutputStream out = context.getContentResolver().openOutputStream(outDoc.getUri())) {
                    if (out != null) out.write(bytes);
                }
                imported++;
            }
        }
        return imported;
    }

    private DocumentFile ensureSafParentDir(DocumentFile baseDir, String entryName) {
        String[] parts = entryName.split("/");
        DocumentFile current = baseDir;
        for (int i = 0; i < parts.length - 1; i++) {
            String p = parts[i];
            if (p == null || p.isEmpty()) continue;
            DocumentFile next = current.findFile(p);
            if (next == null || !next.isDirectory()) {
                next = current.createDirectory(p);
            }
            if (next == null) return null;
            current = next;
        }
        return current;
    }

    private String normalizeZipEntryName(String raw) {
        if (raw == null) return null;
        String name = raw.replace("\\", "/");
        if (name.startsWith("/")) name = name.substring(1);
        if (name.startsWith(BASE_FOLDER + "/")) {
            name = name.substring((BASE_FOLDER + "/").length());
        }
        // Don't allow path traversal
        if (name.contains("..")) return null;
        return name;
    }

    private boolean isSupportedImportEntry(String entryName) {
        return entryName.startsWith(BOOKS_FOLDER + "/")
                || entryName.startsWith(LOANS_FOLDER + "/")
                || entryName.startsWith(BORROWERS_FOLDER + "/")
                || entryName.startsWith(TAGS_FOLDER + "/")
                || entryName.startsWith(THEMES_FOLDER + "/")
                || entryName.startsWith(LIBRARY_FOLDER + "/");
    }

    private boolean looksLikeJson(String entryName) {
        return entryName.endsWith(".bcb") || entryName.endsWith(".bcbr") || entryName.endsWith(".bclo") || entryName.endsWith(".bct");
    }

    private String generateImportFileName(String entryName) {
        String ext = "";
        int dot = entryName.lastIndexOf('.');
        if (dot >= 0) ext = entryName.substring(dot + 1);

        String type = "file";
        if (entryName.startsWith(BOOKS_FOLDER + "/")) type = "book";
        else if (entryName.startsWith(LOANS_FOLDER + "/")) type = "loan";
        else if (entryName.startsWith(BORROWERS_FOLDER + "/")) type = "borrower";
        else if (entryName.startsWith(TAGS_FOLDER + "/")) type = "tag";
        else if (entryName.startsWith(THEMES_FOLDER + "/")) type = "theme";

        return generateFileName(type, ext);
    }

    private byte[] rewriteJsonId(byte[] bytes) {
        try {
            String s = new String(bytes);
            JSONObject obj = new JSONObject(s);
            obj.put("id", ensureId(obj.optLong("id")));
            if (obj.has("dateAdded")) obj.put("dateAdded", System.currentTimeMillis());
            if (obj.has("lastModified")) obj.put("lastModified", System.currentTimeMillis());
            return obj.toString(2).getBytes();
        } catch (Exception e) {
            return bytes;
        }
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }
    
    // JSON conversion helpers
    private JSONObject bookToJson(Book book) throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", book.getId());
        json.put("title", book.getTitle());
        json.put("author", book.getAuthor());
        json.put("isbn", book.getIsbn());
        json.put("publisher", book.getPublisher());
        json.put("edition", book.getEdition());
        json.put("publishYear", book.getPublishYear());
        json.put("genre", book.getGenre());
        json.put("language", book.getLanguage());
        json.put("description", book.getDescription());
        json.put("copies", book.getCopies());
        json.put("availableCopies", book.getAvailableCopies());
        json.put("shelfLocation", book.getShelfLocation());
        json.put("tags", book.getTags());
        json.put("dateAdded", book.getDateAdded());
        json.put("rating", book.getRating());
        json.put("review", book.getReview());
        json.put("readingStatus", book.getReadingStatus() != null ? book.getReadingStatus().name() : "OWN");
        json.put("quickNotes", book.getQuickNotes());
        json.put("readCount", book.getReadCount());
        json.put("customCoverUrl", book.getCustomCoverUrl());
        json.put("lastModified", book.getLastModified());
        json.put("status", book.getStatus());
        return json;
    }
    
    private Book jsonToBook(JSONObject json) {
        Book book = new Book();
        book.setId(json.optLong("id"));
        book.setTitle(json.optString("title"));
        book.setAuthor(json.optString("author"));
        book.setIsbn(json.optString("isbn"));
        book.setPublisher(json.optString("publisher"));
        book.setEdition(json.optString("edition"));
        book.setPublishYear(json.optString("publishYear"));
        book.setGenre(json.optString("genre"));
        book.setLanguage(json.optString("language"));
        book.setDescription(json.optString("description"));
        book.setCopies(json.optInt("copies", 1));
        book.setAvailableCopies(json.optInt("availableCopies", 1));
        book.setShelfLocation(json.optString("shelfLocation"));
        book.setTags(json.optString("tags"));
        book.setDateAdded(json.optLong("dateAdded", System.currentTimeMillis()));
        book.setRating(json.optDouble("rating"));
        book.setReview(json.optString("review"));
        
        String statusStr = json.optString("readingStatus", "OWN");
        try {
            book.setReadingStatus(ReadingStatus.valueOf(statusStr));
        } catch (Exception e) {
            book.setReadingStatus(ReadingStatus.OWN);
        }
        
        book.setQuickNotes(json.optString("quickNotes"));
        book.setReadCount(json.optInt("readCount"));
        book.setCustomCoverUrl(json.optString("customCoverUrl"));
        book.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
        book.setStatus(json.optString("status", "AVAILABLE"));
        return book;
    }
    
    private JSONObject borrowerToJson(Borrower b) throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", b.getId());
        json.put("name", b.getName());
        json.put("email", b.getEmail());
        json.put("phone", b.getPhone());
        json.put("notes", b.getNotes());
        json.put("totalBorrowed", b.getTotalBorrowed());
        json.put("dateAdded", b.getDateAdded());
        json.put("lastModified", b.getLastModified());
        return json;
    }
    
    private Borrower jsonToBorrower(JSONObject json) {
        Borrower b = new Borrower();
        b.setId(json.optLong("id"));
        b.setName(json.optString("name"));
        b.setEmail(json.optString("email"));
        b.setPhone(json.optString("phone"));
        b.setNotes(json.optString("notes"));
        b.setTotalBorrowed(json.optInt("totalBorrowed"));
        b.setDateAdded(json.optLong("dateAdded", System.currentTimeMillis()));
        b.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
        return b;
    }
    
    private JSONObject loanToJson(Loan loan) throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", loan.getId());
        json.put("bookId", loan.getBookId());
        json.put("borrowerId", loan.getBorrowerId());
        json.put("borrowerName", loan.getBorrowerName());
        json.put("loanDate", loan.getLoanDate());
        json.put("dueDate", loan.getDueDate());
        json.put("returnDate", loan.getReturnDate());
        json.put("status", loan.getStatus() != null ? loan.getStatus().name() : "ACTIVE");
        json.put("conditionOnLoan", loan.getConditionOnLoan());
        json.put("conditionOnReturn", loan.getConditionOnReturn());
        json.put("lateFee", loan.getLateFee());
        json.put("notes", loan.getNotes());
        json.put("renewalCount", loan.getRenewalCount());
        json.put("lastModified", loan.getLastModified());
        return json;
    }
    
    private Loan jsonToLoan(JSONObject json) {
        Loan loan = new Loan();
        loan.setId(json.optLong("id"));
        loan.setBookId(json.optLong("bookId"));
        long borrowerIdVal = json.optLong("borrowerId", -1);
        loan.setBorrowerId(borrowerIdVal > 0 ? borrowerIdVal : null);
        loan.setBorrowerName(json.optString("borrowerName"));
        loan.setLoanDate(json.optLong("loanDate"));
        loan.setDueDate(json.optLong("dueDate"));
        loan.setReturnDate(json.optLong("returnDate"));
        
        String statusStr = json.optString("status", "ACTIVE");
        try {
            loan.setStatus(LoanStatus.valueOf(statusStr));
        } catch (Exception e) {
            loan.setStatus(LoanStatus.ACTIVE);
        }
        
        loan.setConditionOnLoan(json.optString("conditionOnLoan"));
        loan.setConditionOnReturn(json.optString("conditionOnReturn"));
        loan.setLateFee(json.optDouble("lateFee"));
        loan.setNotes(json.optString("notes"));
        loan.setRenewalCount(json.optInt("renewalCount"));
        loan.setLastModified(json.optLong("lastModified", System.currentTimeMillis()));
        return loan;
    }
    
    private JSONObject tagToJson(Tag tag) throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", tag.getId());
        json.put("name", tag.getName());
        json.put("color", tag.getColor());
        return json;
    }
    
    private Tag jsonToTag(JSONObject json) {
        Tag tag = new Tag();
        tag.setId(json.optLong("id"));
        tag.setName(json.optString("name"));
        tag.setColor(json.optString("color"));
        return tag;
    }
    
    // Theme operations
    public boolean saveTheme(AppTheme theme) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveThemeViaDocumentFile(theme, baseUri);
            }
            
            JSONObject json = themeToJson(theme);
            File dir = new File(getBasePath(), THEMES_FOLDER);
            File file = theme.getId() > 0 ? findJsonFileById(dir, ".bct", theme.getId()) : null;
            if (file == null) file = new File(dir, generateFileName("theme", "bct"));
            
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean saveThemeViaDocumentFile(AppTheme theme, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            DocumentFile dir = baseDir.findFile(THEMES_FOLDER);
            if (dir == null || !dir.isDirectory()) {
                dir = baseDir.createDirectory(THEMES_FOLDER);
            }
            if (dir == null) return false;
            
            JSONObject json = themeToJson(theme);
            DocumentFile file = theme.getId() > 0 ? findJsonDocFileById(dir, ".bct", theme.getId()) : null;
            if (file == null) {
                file = dir.createFile("application/octet-stream", generateFileName("theme", "bct"));
            }
            if (file == null) return false;
            
            java.io.OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            if (out != null) {
                out.write(json.toString(2).getBytes());
                out.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    public List<AppTheme> loadAllThemes() {
        List<AppTheme> themes = new ArrayList<>();
        try {
            File dir = new File(getBasePath(), THEMES_FOLDER);
            if (dir.exists()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".bct"));
                if (files != null) {
                    for (File file : files) {
                        AppTheme theme = loadTheme(file);
                        if (theme != null) themes.add(theme);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return themes;
    }
    
    public AppTheme loadTheme(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) json.append(line);
            reader.close();
            return jsonToTheme(new JSONObject(json.toString()));
        } catch (Exception e) { return null; }
    }
    
    private JSONObject themeToJson(AppTheme theme) throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", theme.getId());
        json.put("name", theme.getName());
        json.put("css", theme.toCss());
        json.put("primaryColor", theme.getPrimaryColor());
        json.put("secondaryColor", theme.getSecondaryColor());
        json.put("tertiaryColor", theme.getTertiaryColor());
        json.put("backgroundColor", theme.getBackgroundColor());
        json.put("surfaceColor", theme.getSurfaceColor());
        json.put("isDark", theme.isDark());
        json.put("isSystem", theme.isSystem());
        return json;
    }
    
    private AppTheme jsonToTheme(JSONObject json) {
        return new AppTheme(
            json.optLong("id"),
            json.optString("name"),
            json.optInt("primaryColor"),
            json.optInt("secondaryColor"),
            json.optInt("tertiaryColor"),
            json.optInt("backgroundColor"),
            json.optInt("surfaceColor"),
            json.optBoolean("isDark", false),
            json.optBoolean("isSystem", false)
        );
    }
}

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
        return prefs.getString("base_uri", null) != null;
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
                setBaseUri(treeUri.toString());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createBaseFolders() {
        if (getBasePath() == null || getBasePath().isEmpty()) return false;
        
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return createFoldersViaDocumentFile(baseUri);
            }
            
            File baseDir = new File(getBasePath());
            
            boolean allCreated = ensureDirectory(baseDir);
            
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), BOOKS_FOLDER));
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), LOANS_FOLDER));
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), BORROWERS_FOLDER));
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), TAGS_FOLDER));
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), THEMES_FOLDER));
            allCreated = allCreated && ensureDirectory(new File(getBasePath(), LIBRARY_FOLDER));
            
            return checkAllFoldersExist();
        } catch (Exception e) {
            e.printStackTrace();
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
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null || !baseDir.exists()) return false;
            
            String[] folders = {BOOKS_FOLDER, LOANS_FOLDER, BORROWERS_FOLDER, TAGS_FOLDER, THEMES_FOLDER, LIBRARY_FOLDER};
            for (String folderName : folders) {
                DocumentFile folder = baseDir.findFile(folderName);
                if (folder == null || !folder.exists()) {
                    folder = baseDir.createDirectory(folderName);
                    if (folder == null) return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
    
    private String bookIdToFileName(long id, boolean isNew) {
        if (isNew || id <= 0) {
            return generateFileName("book", "bcb");
        }
return "book_" + id + ".bcb";
    }
    
    // Loan operations
    public boolean saveBook(Book book) {
        try {
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveBookViaDocumentFile(book, baseUri);
            }
            
            JSONObject json = bookToJson(book);
            String fileName = generateFileName("book", "bcb");
            File file = new File(new File(getBasePath(), BOOKS_FOLDER), fileName);
            
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
            String fileName = generateFileName("book", "bcb");
            if (!fileName.endsWith(".bcb")) {
                fileName = generateFileName("book", "bcb");
            }
            
            DocumentFile bookFile = booksDir.findFile(fileName);
            if (bookFile == null) {
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
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return deleteBookViaDocumentFile(bookId, baseUri);
            }
            
            File booksDir = new File(getBasePath(), BOOKS_FOLDER);
            File[] files = booksDir.listFiles((dir, name) -> name.contains(bookId));
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean deleteBookViaDocumentFile(String bookId, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            DocumentFile baseDir = DocumentFile.fromTreeUri(context, uri);
            if (baseDir == null) return false;
            
            DocumentFile booksDir = baseDir.findFile(BOOKS_FOLDER);
            if (booksDir == null || !booksDir.isDirectory()) return false;
            
            for (DocumentFile file : booksDir.listFiles()) {
                if (file.getName() != null && file.getName().contains(bookId)) {
                    file.delete();
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
            String baseUri = prefs.getString("base_uri", null);
            if (baseUri != null) {
                return saveBorrowerViaDocumentFile(borrower, baseUri);
            }
            
            JSONObject json = borrowerToJson(borrower);
            String fileName = generateFileName("borrower", "bcbr");
            File file = new File(new File(getBasePath(), BORROWERS_FOLDER), fileName);
            
            if (!file.getName().endsWith(".bcbr")) {
                file = new File(new File(getBasePath(), BORROWERS_FOLDER), 
                    generateFileName("borrower", "bcbr"));
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
            String fileName = generateFileName("borrower", "bcbr");
            if (!fileName.endsWith(".bcbr")) {
                fileName = generateFileName("borrower", "bcbr");
            }
            
            DocumentFile file = dir.findFile(fileName);
            if (file == null) {
                file = dir.createFile("application/octet-stream", fileName);
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
            String fileName = loan.getId() > 0 ? "loan_" + loan.getId() + ".bclo" : generateFileName("loan", "bclo");
            if (!fileName.endsWith(".bclo")) {
                fileName = generateFileName("loan", "bclo");
            }
            
            DocumentFile file = dir.findFile(fileName);
            if (file == null) {
                file = dir.createFile("application/octet-stream", fileName);
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
            
            JSONObject json = loanToJson(loan);
            String fileName = loan.getId() > 0 ? "loan_" + loan.getId() + ".bclo" : generateFileName("loan", "bclo");
            File file = new File(new File(getBasePath(), LOANS_FOLDER), fileName);
            
            if (!file.getName().endsWith(".bclo")) {
                file = new File(new File(getBasePath(), LOANS_FOLDER), 
                    generateFileName("loan", "bclo"));
            }
            
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
            
            JSONObject json = tagToJson(tag);
            String fileName = tag.getId() > 0 ? "tag_" + tag.getId() + ".bct" : generateFileName("tag", "bct");
            File file = new File(new File(getBasePath(), TAGS_FOLDER), fileName);
            
            if (!file.getName().endsWith(".bct")) {
                file = new File(new File(getBasePath(), TAGS_FOLDER), 
                    generateFileName("tag", "bct"));
            }
            
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
            
            JSONObject json = tagToJson(tag);
            String fileName = tag.getId() > 0 ? "tag_" + tag.getId() + ".bct" : generateFileName("tag", "bct");
            if (!fileName.endsWith(".bct")) {
                fileName = generateFileName("tag", "bct");
            }
            
            DocumentFile file = dir.findFile(fileName);
            if (file == null) {
                file = dir.createFile("application/octet-stream", fileName);
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
            File baseDir = new File(getBasePath());
            File exportDir = new File(baseDir, LIBRARY_FOLDER);
            if (!exportDir.exists()) exportDir.mkdirs();
            
            String timestamp = DATE_FORMAT.format(new Date());
            String random = String.format("%04d", new Random().nextInt(10000));
            String fullName = "bcl" + timestamp + random + ".bcl";
            File zipFile = new File(exportDir, fullName);
            
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            addFolderToZip(zos, baseDir, baseDir);
            zos.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
            String fileName = theme.getId() > 0 ? "theme_" + theme.getId() + ".bct" : generateFileName("theme", "bct");
            File file = new File(new File(getBasePath(), THEMES_FOLDER), fileName);
            
            if (!file.getName().endsWith(".bct")) {
                file = new File(new File(getBasePath(), THEMES_FOLDER), 
                    generateFileName("theme", "bct"));
            }
            
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
            String fileName = theme.getId() > 0 ? "theme_" + theme.getId() + ".bct" : generateFileName("theme", "bct");
            if (!fileName.endsWith(".bct")) {
                fileName = generateFileName("theme", "bct");
            }
            
            DocumentFile file = dir.findFile(fileName);
            if (file == null) {
                file = dir.createFile("application/octet-stream", fileName);
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

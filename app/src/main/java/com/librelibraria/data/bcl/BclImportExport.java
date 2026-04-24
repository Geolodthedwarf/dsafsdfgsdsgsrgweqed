package com.librelibraria.data.bcl;

import android.content.Context;
import com.librelibraria.data.model.Book;
import com.librelibraria.data.model.Borrower;
import com.librelibraria.data.model.Loan;
import com.librelibraria.data.model.LoanStatus;
import com.librelibraria.data.model.ReadingStatus;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BclImportExport {
    
    private static final String FILE_EXTENSION = ".bcl";
    private static final String FILE_HEADER = "LIBRELIBRARIA_V1";
    
    public interface ImportCallback {
        void onSuccess(int booksImported, int borrowersImported);
        void onError(String message);
    }
    
    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String message);
    }
    
    public static void importFile(Context context, File file, ImportCallback callback) {
        try {
            List<Book> books = new ArrayList<>();
            List<Borrower> borrowers = new ArrayList<>();
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String header = reader.readLine();
            
            if (!header.startsWith(FILE_HEADER)) {
                callback.onError("Invalid file format");
                return;
            }
            
            String line;
            boolean inBooks = false;
            boolean inBorrowers = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[BOOKS]")) {
                    inBooks = true;
                    inBorrowers = false;
                } else if (line.startsWith("[BORROWERS]")) {
                    inBooks = false;
                    inBorrowers = true;
                } else if (line.startsWith("[END]")) {
                    break;
                } else if (line.trim().isEmpty()) {
                    continue;
                } else {
                    String[] parts = line.split("\\|");
                    if (inBooks && parts.length >= 3) {
                        Book book = new Book();
                        book.setTitle(parts[0]);
                        book.setAuthor(parts.length > 1 ? parts[1] : "");
                        book.setIsbn(parts.length > 2 ? parts[2] : "");
                        book.setReadingStatus(ReadingStatus.OWN);
                        book.setAvailableCopies(1);
                        books.add(book);
                    } else if (inBorrowers && parts.length >= 2) {
                        Borrower borrower = new Borrower();
                        borrower.setName(parts[0]);
                        borrower.setEmail(parts.length > 1 ? parts[1] : "");
                        borrowers.add(borrower);
                    }
                }
            }
            
            reader.close();
            callback.onSuccess(books.size(), borrowers.size());
            
        } catch (Exception e) {
            callback.onError("Import failed: " + e.getMessage());
        }
    }
    
    public static void exportData(Context context, List<Book> books, List<Borrower> borrowers, 
                            String fileName, ExportCallback callback) {
        try {
            File exportDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            File exportFile = new File(exportDir, fileName + "_" + timestamp + FILE_EXTENSION);
            
            FileWriter writer = new FileWriter(exportFile);
            writer.write(FILE_HEADER + "\n\n");
            
            writer.write("[BOOKS]\n");
            for (Book book : books) {
                writer.write(book.getTitle() + "|" + book.getAuthor() + "|" + book.getIsbn() + "\n");
            }
            
            writer.write("\n[BORROWERS]\n");
            for (Borrower borrower : borrowers) {
                writer.write(borrower.getName() + "|" + borrower.getEmail() + "\n");
            }
            
            writer.write("\n[END]\n");
            writer.close();
            
            callback.onSuccess(exportFile.getAbsolutePath());
            
        } catch (IOException e) {
            callback.onError("Export failed: " + e.getMessage());
        }
    }
}
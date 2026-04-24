package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CloudBackupService {

    private final FileStorageManager storageManager;
    private final Context context;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    public CloudBackupService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<String> createBackup() {
        return Single.fromCallable(() -> {
            String basePath = storageManager.getBasePath();
            if (basePath == null) {
                throw new Exception("No library folder selected");
            }

            File baseDir = new File(basePath);
            String timestamp = DATE_FORMAT.format(new Date());
            String backupName = "librelibraria_backup_" + timestamp + ".lbb";
            
            File backupsDir = new File(context.getExternalFilesDir(null), "backups");
            if (!backupsDir.exists()) backupsDir.mkdirs();
            
            File backupFile = new File(backupsDir, backupName);
            
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile));
            addToZip(zos, baseDir, baseDir);
            zos.close();
            
            return backupFile.getAbsolutePath();
        }).subscribeOn(Schedulers.io());
    }

    public Single<String> restoreBackup(String backupFilePath) {
        return Single.fromCallable(() -> {
            String basePath = storageManager.getBasePath();
            if (basePath == null) {
                throw new Exception("No library folder selected");
            }

            File backupFile = new File(backupFilePath);
            if (!backupFile.exists()) {
                throw new Exception("Backup file not found");
            }

            // Create backup of current data first
            String timestamp = DATE_FORMAT.format(new Date());
            File preRestoreBackup = new File(context.getExternalFilesDir(null), 
                "backups/pre_restore_" + timestamp + ".lbb");
            createBackupFile(basePath, preRestoreBackup.getAbsolutePath());

            // Extract backup
            ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile));
            ZipEntry entry;
            File baseDir = new File(basePath);
            
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = newFile(baseDir, entry);
                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    FileOutputStream fos = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zis.closeEntry();
            }
            zis.close();
            
            return "Backup restored successfully";
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<BackupInfo>> listBackups() {
        return Single.fromCallable(() -> {
            List<BackupInfo> backups = new java.util.ArrayList<>();
            File backupsDir = new File(context.getExternalFilesDir(null), "backups");
            if (backupsDir.exists()) {
                File[] files = backupsDir.listFiles((d, name) -> name.endsWith(".lbb"));
                if (files != null) {
                    for (File f : files) {
                        backups.add(new BackupInfo(f.getName(), f.length(), f.lastModified()));
                    }
                }
            }
            backups.sort((a, b) -> Long.compare(b.date, a.date));
            return backups;
        }).subscribeOn(Schedulers.io());
    }

    public Completable deleteBackup(String backupName) {
        return Completable.fromAction(() -> {
            File backupsDir = new File(context.getExternalFilesDir(null), "backups");
            File backupFile = new File(backupsDir, backupName);
            if (backupFile.exists()) {
                backupFile.delete();
            }
        }).subscribeOn(Schedulers.io());
    }

    private void addToZip(ZipOutputStream zos, File sourceFile, File baseDir) throws IOException {
        if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    addToZip(zos, file, baseDir);
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

    private void createBackupFile(String sourcePath, String destPath) throws IOException {
        File source = new File(sourcePath);
        File dest = new File(destPath);
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest));
        addToZip(zos, source, source);
        zos.close();
    }

    private File newFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());
        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    public static class BackupInfo {
        public final String name;
        public final long size;
        public final long date;

        public BackupInfo(String name, long size, long date) {
            this.name = name;
            this.size = size;
            this.date = date;
        }

        public String getSizeFormatted() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
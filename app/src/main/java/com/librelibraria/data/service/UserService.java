package com.librelibraria.data.service;

import android.content.Context;
import android.content.SharedPreferences;
import com.librelibraria.data.model.User;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_CURRENT_USER = "current_user_id";

    private final FileStorageManager storageManager;
    private final SharedPreferences prefs;
    private final Context context;

    public UserService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Single<List<User>> getAllUsers() {
        return Single.fromCallable(() -> loadAllUsers())
                .subscribeOn(Schedulers.io());
    }

    public Single<User> getCurrentUser() {
        return Single.fromCallable(() -> {
            long userId = prefs.getLong(KEY_CURRENT_USER, 1);
            List<User> users = loadAllUsers();
            for (User u : users) {
                if (u.getId() == userId) {
                    return u;
                }
            }
            // Return first user or create default
            if (!users.isEmpty()) {
                return users.get(0);
            }
            User defaultUser = new User("Default User", "user@local");
            defaultUser.setId(1);
            return defaultUser;
        }).subscribeOn(Schedulers.io());
    }

    public Completable setCurrentUser(long userId) {
        return Completable.fromAction(() -> 
            prefs.edit().putLong(KEY_CURRENT_USER, userId).apply())
            .subscribeOn(Schedulers.io());
    }

    public Completable saveUser(User user) {
        return Completable.fromAction(() -> {
            if (user.getId() == 0) {
                user.setId(System.currentTimeMillis());
            }
            saveUserToFile(user);
        }).subscribeOn(Schedulers.io());
    }

    public Completable deleteUser(long userId) {
        return Completable.fromAction(() -> {
            String path = storageManager.getBasePath();
            if (path != null) {
                File userFile = new File(path + "/users/user_" + userId + ".usr");
                if (userFile.exists()) {
                    userFile.delete();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public boolean isMultiUserEnabled() {
        List<User> users = loadAllUsers();
        return users.size() > 1;
    }

    private List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        String path = storageManager.getBasePath();
        if (path == null) return users;

        File usersDir = new File(path + "/users");
        if (!usersDir.exists()) {
            usersDir.mkdirs();
            // Create default user
            User defaultUser = new User("Default User", "user@local");
            defaultUser.setId(1);
            defaultUser.setRole("ADMIN");
            saveUserToFile(defaultUser);
            users.add(defaultUser);
            return users;
        }

        File[] files = usersDir.listFiles((d, name) -> name.endsWith(".usr"));
        if (files != null) {
            for (File file : files) {
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                    reader.close();
                    users.add(parseUser(json.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return users;
    }

    private void saveUserToFile(User user) {
        String path = storageManager.getBasePath();
        if (path == null) return;

        File usersDir = new File(path + "/users");
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }

        try {
            File file = new File(usersDir, "user_" + user.getId() + ".usr");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(userToJson(user));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String userToJson(User u) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"avatar\":\"%s\",\"isActive\":%b,\"createdAt\":%d,\"role\":\"%s\"}",
            u.getId(), escape(u.getName()), escape(u.getEmail()), 
            u.getAvatar() != null ? u.getAvatar() : "", 
            u.isActive(), u.getCreatedAt(), u.getRole()
        );
    }

    private User parseUser(String json) {
        User u = new User();
        try {
            json = json.replace("{", "").replace("}", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length != 2) continue;
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");

                switch (key) {
                    case "id": u.setId(Long.parseLong(value)); break;
                    case "name": u.setName(value); break;
                    case "email": u.setEmail(value); break;
                    case "avatar": u.setAvatar(value); break;
                    case "isActive": u.setActive(Boolean.parseBoolean(value)); break;
                    case "createdAt": u.setCreatedAt(Long.parseLong(value)); break;
                    case "role": u.setRole(value); break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
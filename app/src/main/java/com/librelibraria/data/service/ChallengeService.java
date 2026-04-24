package com.librelibraria.data.service;

import android.content.Context;
import com.librelibraria.data.model.Challenge;
import com.librelibraria.data.storage.FileStorageManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChallengeService {

    private final FileStorageManager storageManager;
    private final Context context;

    public ChallengeService(Context context) {
        this.context = context.getApplicationContext();
        this.storageManager = FileStorageManager.getInstance(context);
    }

    public Single<List<Challenge>> getAllChallenges() {
        return Single.fromCallable(() -> loadAllChallenges())
                .subscribeOn(Schedulers.io());
    }

    public Single<Challenge> getActiveChallenge() {
        return getAllChallenges()
                .map(challenges -> {
                    for (Challenge c : challenges) {
                        if (c.isActive()) return c;
                    }
                    return null;
                });
    }

    public Completable saveChallenge(Challenge challenge) {
        return Completable.fromAction(() -> {
            if (challenge.getId() == 0) {
                challenge.setId(System.currentTimeMillis());
            }
            saveChallengeToFile(challenge);
        }).subscribeOn(Schedulers.io());
    }

    public Completable completeChallenge(long challengeId, int bookPages) {
        return Completable.fromAction(() -> {
            List<Challenge> challenges = loadAllChallenges();
            for (Challenge c : challenges) {
                if (c.getId() == challengeId) {
                    c.incrementBook(bookPages);
                    c.setActive(false);
                    c.setStatus("COMPLETED");
                    saveChallengeToFile(c);
                    break;
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable deleteChallenge(long challengeId) {
        return Completable.fromAction(() -> {
            String path = storageManager.getBasePath();
            if (path != null) {
                File challengeFile = new File(path + "/challenges/challenge_" + challengeId + ".chc");
                if (challengeFile.exists()) {
                    challengeFile.delete();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private List<Challenge> loadAllChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        String path = storageManager.getBasePath();
        if (path == null) return challenges;

        File challengesDir = new File(path + "/challenges");
        if (!challengesDir.exists()) {
            challengesDir.mkdirs();
        }

        File[] files = challengesDir.listFiles((d, name) -> name.endsWith(".chc"));
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
                    challenges.add(parseChallengeFromJson(json.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return challenges;
    }

    private void saveChallengeToFile(Challenge challenge) {
        String path = storageManager.getBasePath();
        if (path == null) return;

        File challengesDir = new File(path + "/challenges");
        if (!challengesDir.exists()) {
            challengesDir.mkdirs();
        }

        try {
            File file = new File(challengesDir, "challenge_" + challenge.getId() + ".chc");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(challengeToJson(challenge));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String challengeToJson(Challenge c) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"id\": ").append(c.getId()).append(",\n");
        json.append("  \"title\": \"").append(escapeJson(c.getTitle())).append("\",\n");
        json.append("  \"goalBooks\": ").append(c.getGoalBooks()).append(",\n");
        json.append("  \"goalPages\": ").append(c.getGoalPages()).append(",\n");
        json.append("  \"startDate\": ").append(c.getStartDate()).append(",\n");
        json.append("  \"endDate\": ").append(c.getEndDate()).append(",\n");
        json.append("  \"completedBooks\": ").append(c.getCompletedBooks()).append(",\n");
        json.append("  \"completedPages\": ").append(c.getCompletedPages()).append(",\n");
        json.append("  \"active\": ").append(c.isActive()).append(",\n");
        json.append("  \"status\": \"").append(c.getStatus()).append("\"\n");
        json.append("}");
        return json.toString();
    }

    private Challenge parseChallengeFromJson(String json) {
        Challenge c = new Challenge();
        try {
            json = json.trim();
            json = json.replace("{", "").replace("}", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length != 2) continue;
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");

                switch (key) {
                    case "id": c.setId(Long.parseLong(value)); break;
                    case "title": c.setTitle(value); break;
                    case "goalBooks": c.setGoalBooks(Integer.parseInt(value)); break;
                    case "goalPages": c.setGoalPages(Integer.parseInt(value)); break;
                    case "startDate": c.setStartDate(Long.parseLong(value)); break;
                    case "endDate": c.setEndDate(Long.parseLong(value)); break;
                    case "completedBooks": c.setCompletedBooks(Integer.parseInt(value)); break;
                    case "completedPages": c.setCompletedPages(Integer.parseInt(value)); break;
                    case "active": c.setActive(Boolean.parseBoolean(value)); break;
                    case "status": c.setStatus(value); break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
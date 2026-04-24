package com.librelibraria.data.service;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;

public class ReadingStreakService {

    private static final String PREFS_NAME = "streak_prefs";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_LONGEST_STREAK = "longest_streak";
    private static final String KEY_LAST_ACTIVE_DATE = "last_active_date";
    private static final String KEY_TOTAL_ACTIVE_DAYS = "total_active_days";

    private final SharedPreferences prefs;
    private final Context context;

    public ReadingStreakService(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getCurrentStreak() {
        if (!wasActiveToday() && !wasActiveYesterday()) {
            return 0;
        }
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    public int getLongestStreak() {
        return prefs.getInt(KEY_LONGEST_STREAK, 0);
    }

    public int getTotalActiveDays() {
        return prefs.getInt(KEY_TOTAL_ACTIVE_DAYS, 0);
    }

    public boolean wasActiveToday() {
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE_DATE, 0);
        if (lastActive == 0) return false;
        
        long today = getStartOfDay(System.currentTimeMillis());
        return lastActive >= today;
    }

    public boolean wasActiveYesterday() {
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE_DATE, 0);
        if (lastActive == 0) return false;
        
        long today = getStartOfDay(System.currentTimeMillis());
        long yesterday = today - TimeUnit.DAYS.toMillis(1);
        return lastActive >= yesterday && lastActive < today;
    }

    public void recordActivity() {
        long now = System.currentTimeMillis();
        long today = getStartOfDay(now);
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE_DATE, 0);
        
        if (lastActive >= today) {
            return; // Already recorded today
        }
        
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);
        
        if (wasActiveYesterday() || currentStreak == 0) {
            // Continue or start new streak
            currentStreak++;
        } else {
            // Streak broken, start new
            currentStreak = 1;
        }
        
        int longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0);
        int totalDays = prefs.getInt(KEY_TOTAL_ACTIVE_DAYS, 0);
        
        // Update records
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, Math.max(currentStreak, longestStreak))
            .putInt(KEY_TOTAL_ACTIVE_DAYS, totalDays + 1)
            .putLong(KEY_LAST_ACTIVE_DATE, now)
            .apply();
    }

    public String getStreakEmoji() {
        int streak = getCurrentStreak();
        if (streak >= 30) return "🏆";
        if (streak >= 14) return "🔥";
        if (streak >= 7) return "⚡";
        if (streak >= 3) return "✨";
        if (streak >= 1) return "📖";
        return "💤";
    }

    public String getBadge() {
        int streak = getCurrentStreak();
        if (streak >= 365) return "Legendary Reader";
        if (streak >= 100) return "Master Reader";
        if (streak >= 30) return "Dedicated Reader";
        if (streak >= 14) return "Book Enthusiast";
        if (streak >= 7) return "Week Warrior";
        if (streak >= 3) return "Reading Rookie";
        return "Getting Started";
    }

    private long getStartOfDay(long timestamp) {
        long millisPerDay = TimeUnit.DAYS.toMillis(1);
        return (timestamp / millisPerDay) * millisPerDay;
    }
}
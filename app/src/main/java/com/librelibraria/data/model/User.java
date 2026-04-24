package com.librelibraria.data.model;

public class User {
    private long id;
    private String name;
    private String email;
    private String avatar;
    private boolean isActive;
    private long createdAt;
    private long lastActive;
    private String role; // ADMIN, MEMBER

    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.role = "MEMBER";
    }

    public User(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
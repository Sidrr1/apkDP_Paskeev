package com.example.appdp;



public class Plan {
    private int id;
    private String addictionType;
    private String title;
    private String description;
    private int durationDays;
    private boolean isCustom;
    private int userId;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAddictionType() { return addictionType; }
    public void setAddictionType(String addictionType) { this.addictionType = addictionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
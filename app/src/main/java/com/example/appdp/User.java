package com.example.appdp;


public class User {
    private int id;
    private String email;
    private String passwordHash;
    private String addictionType;
    private String startDate;
    private int daysCounter;
    private String lastClickDate;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAddictionType() { return addictionType; }
    public void setAddictionType(String addictionType) { this.addictionType = addictionType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public int getDaysCounter() { return daysCounter; }
    public void setDaysCounter(int daysCounter) { this.daysCounter = daysCounter; }

    public String getLastClickDate() { return lastClickDate; }
    public void setLastClickDate(String lastClickDate) { this.lastClickDate = lastClickDate; }
}
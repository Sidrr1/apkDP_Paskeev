package com.example.appdp;



public class Article {
    private int id;
    private String addictionType;
    private String title;
    private String description;
    private String content;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAddictionType() { return addictionType; }
    public void setAddictionType(String addictionType) { this.addictionType = addictionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
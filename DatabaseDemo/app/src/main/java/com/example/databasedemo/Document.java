package com.example.databasedemo;

public class Document {
    private int id;
    private String name, subject;
    private int wordCount;
    private String textDate;

    public Document(int id, String name, String subject, int wordCount, String textDate) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.wordCount = wordCount;
        this.textDate = textDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public int getWordCount() {
        return wordCount;
    }

    public String getTextDate() {
        return textDate;
    }
}

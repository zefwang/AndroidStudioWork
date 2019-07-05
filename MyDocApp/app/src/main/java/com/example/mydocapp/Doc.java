package com.example.mydocapp;

public class Doc {
    private int id;
    private String name, subject;
    private int entryDate;

    public Doc(int id, String name, String subject, int entryDate) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.entryDate = entryDate;
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

    public int getEntryDate() {
        return entryDate;
    }
}

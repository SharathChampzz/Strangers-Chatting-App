package com.example.backgroundloc;

public class Report {
    private String username, message;

    public Report() {
    }

    public Report(String disease, String message) {
        this.username = disease;
        this.message = message;
    }

    public String getDisease() {
        return username;
    }

    public void setDisease(String disease) {
        this.username = disease;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

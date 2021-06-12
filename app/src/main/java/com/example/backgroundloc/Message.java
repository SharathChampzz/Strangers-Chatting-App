package com.example.backgroundloc;

public class Message {
    private String message, phonenumber;

    public Message() {
    }

    public Message(String message, String phonenumber) {
        this.message = message;
        this.phonenumber = phonenumber;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

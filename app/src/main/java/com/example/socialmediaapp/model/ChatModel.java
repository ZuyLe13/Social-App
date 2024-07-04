package com.example.socialmediaapp.model;

public class ChatModel {
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private boolean isImage;  // Đảm bảo có thuộc tính isImage

    public ChatModel(String sender, String receiver, String message, boolean isseen, boolean isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.isImage = isImage;
    }

    public ChatModel() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public boolean getIsImage() {  // Sửa tên phương thức getter
        return isImage;
    }

    public void setIsImage(boolean isImage) {  // Sửa tên phương thức setter
        this.isImage = isImage;
    }
}



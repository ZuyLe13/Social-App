package com.example.socialmediaapp.model;

public class NotificationModel {
    String pId, timestamp, pUId, notification, sUid, sName, sImage;

    public NotificationModel() {
    }

    public NotificationModel(String pId, String timestamp, String pUId, String notification, String sUid, String sName, String sImage) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.pUId = pUId;
        this.notification = notification;
        this.sUid = sUid;
        this.sName = sName;
        this.sImage = sImage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getpUId() {
        return pUId;
    }

    public void setpUId(String pUId) {
        this.pUId = pUId;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getsUid() {
        return sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }
}

package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class PostModel {

    private String imageUrl, id, description, uid;
    @ServerTimestamp
    private Date timeStamp;

    public PostModel(){

    }

    public PostModel(String imageUrl, String id, String description, Date timeStamp, String uid) {
        this.imageUrl = imageUrl;
        this.id = id;
        this.timeStamp = timeStamp;
        this.description = description;
        this.uid = uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

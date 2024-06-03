package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class HomeModel {
    private String name, profileImage, imageUrl, uid, comments, description, id;
    private List<String> reactList;
    @ServerTimestamp
    private Date timeStamp;

    public HomeModel(){

    }

    public HomeModel(String name, String profileImage, String imageUrl, String uid, String comments, String description, String id, List<String> reactList, Date timeStamp) {
        this.name = name;
        this.profileImage = profileImage;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.comments = comments;
        this.description = description;
        this.id = id;
        this.reactList = reactList;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setUserName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getReactList() {
        return reactList;
    }

    public void setReactList(List<String> reactList) {
        this.reactList = reactList;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
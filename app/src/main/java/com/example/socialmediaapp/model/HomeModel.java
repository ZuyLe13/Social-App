package com.example.socialmediaapp.model;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeModel {
    private String name, profileImage, imageUrl, uid, description, id;
    private int commentCount;
    private List<String> likes = new ArrayList<>(), hahas = new ArrayList<>(), sads = new ArrayList<>(), wows = new ArrayList<>(), angrys = new ArrayList<>();
    @ServerTimestamp
    private Date timeStamp;

    public HomeModel(){

    }

    public HomeModel(String name, String profileImage, String imageUrl, String uid, String description, String id, List<String> likes, List<String> hahas, List<String> sads, List<String> wows, List<String> angrys, Date timeStamp, int commentCount) {
        this.name = name;
        this.profileImage = profileImage;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.description = description;
        this.id = id;
        this.likes = likes;
        this.hahas = hahas;
        this.sads = sads;
        this.wows = wows;
        this.angrys = angrys;
        this.timeStamp = timeStamp;
        this.commentCount = commentCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public List<String> getHahas() {
        return hahas;
    }

    public void setHahas(List<String> hahas) {
        this.hahas = hahas;
    }

    public List<String> getSads() {
        return sads;
    }

    public void setSads(List<String> sads) {
        this.sads = sads;
    }

    public List<String> getWows() {
        return wows;
    }

    public void setWows(List<String> wows) {
        this.wows = wows;
    }

    public List<String> getAngrys() {
        return angrys;
    }

    public void setAngrys(List<String> angrys) {
        this.angrys = angrys;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
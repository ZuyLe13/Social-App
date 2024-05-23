package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentModel {
    private String id, postid, uid, comment, name, avt;
    @ServerTimestamp
    private Date timestamp;
    public CommentModel(){

    }

    public CommentModel(String id, String postid, String uid, String comment, String name, String avt, Date timestamp) {
        this.id = id;
        this.postid = postid;
        this.uid = uid;
        this.comment = comment;
        this.name = name;
        this.avt = avt;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvt() {
        return avt;
    }

    public void setAvt(String avt) {
        this.avt = avt;
    }
}

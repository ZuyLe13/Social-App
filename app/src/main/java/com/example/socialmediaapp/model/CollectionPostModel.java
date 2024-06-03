package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class CollectionPostModel {
    private String id, postImg, postid, uid, uname;
    @ServerTimestamp
    private Date postTimestamp;

    public CollectionPostModel(){
    }

    public CollectionPostModel(String id, String postImg, String postid, String uid, String uname, Date postTimestamp) {
        this.id = id;
        this.postImg = postImg;
        this.postid = postid;
        this.uid = uid;
        this.uname = uname;
        this.postTimestamp = postTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
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

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public Date getPostTimestamp() {
        return postTimestamp;
    }

    public void setPostTimestamp(Date postTimestamp) {
        this.postTimestamp = postTimestamp;
    }
}

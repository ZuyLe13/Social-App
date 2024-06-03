package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class CollectionModel {
    private String id, name, description;
    private int postCount;
    @ServerTimestamp
    private Date timestamp;

    public CollectionModel(){

    }

    public CollectionModel(String id, String name, String description, int postCount, Date timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.postCount = postCount;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

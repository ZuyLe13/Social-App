package com.example.socialmediaapp.model;

public class UserModel {
    private String email, name, profileImg, status, uID, search;

    public UserModel(){

    }

    public UserModel(String email, String name, String profileImg, String status, String uID, String search) {
        this.email = email;
        this.name = name;
        this.profileImg = profileImg;
        this.status = status;
        this.uID = uID;
        this.search = search;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}

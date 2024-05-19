package com.example.socialmediaapp.model;

import android.net.Uri;

public class GalleryImageModel {
    private Uri picUri;

    public GalleryImageModel(){

    }

    public GalleryImageModel(Uri picUri) {
        this.picUri = picUri;
    }

    public Uri getPicUri() {
        return picUri;
    }

    public void setPicUri(Uri picUri) {
        this.picUri = picUri;
    }


}

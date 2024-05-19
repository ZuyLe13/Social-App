package com.example.socialmediaapp.utils;

import android.net.Uri;

import com.example.socialmediaapp.model.GalleryImageModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageContent {
    static final List<GalleryImageModel> galleryList = new ArrayList<>();
    public static  void loadImages(File file){
        GalleryImageModel images = new GalleryImageModel();
        images.setPicUri(Uri.fromFile(file));
        addImages(images);
    }

    private static void addImages(GalleryImageModel images) {
        galleryList.add(0, images);
    }

    public static void loadSavedImages(File directory){
        galleryList.clear();
        if  (directory.exists()){
            File[] files = directory.listFiles();
            for (File file : files){
                String absolutePath = file.getAbsolutePath();
                String extension = absolutePath.substring((absolutePath.lastIndexOf(".")));

                if (extension.equals(".jpg") || extension.equals(".png")){
                    loadImages(file);
                }
            }
        }
    }
}

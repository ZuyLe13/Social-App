package com.example.socialmediaapp.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.GalleryImageModel;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryHolder> {

    private List<GalleryImageModel> galleryList;

    private SendImage onSendImage;

    public GalleryAdapter(List<GalleryImageModel> galleryList){
        this.galleryList = galleryList;
    }

    @NonNull
    @Override
    public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new GalleryHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull GalleryHolder holder, @SuppressLint("RecyclerView") final int position) {

        Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(galleryList.get(position).getPicUri())
                        .into(holder.postImageView);
        holder.postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(galleryList.get(position).getPicUri());
            }
        });
    }

    private void chooseImage(Uri picUri) {
        onSendImage.onSend(picUri);

    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    static class GalleryHolder extends RecyclerView.ViewHolder{

        private ImageView postImageView;

        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
        }
    }

    public interface SendImage {
        void onSend(Uri picUri);
    }

    public void SendImage (SendImage sendImage){
        this.onSendImage = sendImage;
    }
}

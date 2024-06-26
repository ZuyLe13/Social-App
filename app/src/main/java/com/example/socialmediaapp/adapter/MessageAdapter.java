package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.ChatActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<ChatModel> mChat;
    private String imgURL;
    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<ChatModel> mChat, String imgURL){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imgURL = imgURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatting_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatting_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chat = mChat.get(position);
        holder.showMsg.setText(chat.getMessage());

        if (imgURL.equals("")) {
            holder.profileImg.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imgURL).into(holder.profileImg);
        }
        if (position == mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            }
            else{
                holder.txt_seen.setText("Sent");
            }
        }
        else{
            holder.txt_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMsg;
        public ImageView profileImg;
        public TextView txt_seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMsg = itemView.findViewById(R.id.show_msg);
            profileImg = itemView.findViewById(R.id.userAvt);
            txt_seen = itemView.findViewById(R.id.txt_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}

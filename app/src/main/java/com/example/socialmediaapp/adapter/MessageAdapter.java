package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<ChatModel> mChat;
    private String imgURL;
    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<ChatModel> mChat, String imgURL) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imgURL = imgURL;
    }
    public void setImageURL(String imgURL) {
        this.imgURL = imgURL;
        notifyDataSetChanged();
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
        Log.d("MessageAdapter", "Message: " + chat.getMessage() + ", isImage: " + chat.getIsImage());

        String timeStamp = chat.getTimestamp();
        if (timeStamp != null && !timeStamp.isEmpty()) {
            try {
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(timeStamp));
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
                String date = sdfDate.format(cal.getTime());
                String time = sdfTime.format(cal.getTime());

                Calendar currentCal = Calendar.getInstance();
                String currentDate = sdfDate.format(currentCal.getTime());

                if (date.equals(currentDate)) {
                    holder.timeTV.setText(time);
                } else {
                    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.ENGLISH);
                    String datetime = sdfDateTime.format(cal.getTime());
                    holder.timeTV.setText(datetime);
                }
            } catch (NumberFormatException e) {
                holder.timeTV.setText("Invalid date");
            }
        } else {
            holder.timeTV.setText("");
        }

        if (chat.getIsImage()) {
            holder.showMsg.setVisibility(View.GONE);
            holder.chat_image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(chat.getMessage()).into(holder.chat_image);
            Log.d("MessageAdapter", "Displaying image message");
        } else {
            holder.showMsg.setText(chat.getMessage());
            holder.showMsg.setVisibility(View.VISIBLE);
            holder.chat_image.setVisibility(View.GONE);
            Log.d("MessageAdapter", "Displaying text message");
        }

        if (imgURL == null || imgURL.isEmpty()) {
            holder.profileImg.setImageResource(R.mipmap.ic_launcher);
            Log.d("imgURL", "nope");
        } else {
            Glide.with(mContext).load(imgURL).into(holder.profileImg);
            Log.d("imgURL", "yes");
        }

        if (position == mChat.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Sent");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMsg, txt_seen, timeTV;
        public CircleImageView profileImg;
        public ImageView chat_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMsg = itemView.findViewById(R.id.show_msg);
            profileImg = itemView.findViewById(R.id.userAvt);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            chat_image = itemView.findViewById(R.id.chat_image);
            timeTV = itemView.findViewById(R.id.timeTV);
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

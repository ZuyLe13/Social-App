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
import com.example.socialmediaapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {
    private Context mContext;
    private List<UserModel> mUsers;
    private boolean isActive;
    String thelastMsg;
    public UserChatAdapter(Context mContext, List<UserModel> mUsers, boolean isActive){
        this.mUsers=mUsers;
        this.mContext = mContext;
        this.isActive = isActive;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_user,
                parent,false);
        return new UserChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = mUsers.get(position);
        holder.username.setText(user.getName());
        if (Objects.equals(user.getProfileImg(), "")){
            holder.profileImg.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Glide.with(mContext).load(user.getProfileImg()).into(holder.profileImg);
        }
        Log.d("UserChatAdapter", "User: " + user.getName() + " Status: " + user.getStatus());
        if (isActive){
            lastMessage(user.getuID(), holder.last_msg);
        }
        else{
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isActive){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
                Log.d("isActive", "1");

            }else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
                Log.d("isActive", "2");

            }
        }else{
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
            Log.d("isActive", "3");

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("uID",user.getuID());
                mContext.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {

        Log.d("UserChatAdapter", "Item count: " + mUsers.size());
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public CircleImageView profileImg;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.userName);
            profileImg = itemView.findViewById(R.id.userAvt);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }
    private void lastMessage (String userid, TextView last_msg){
        thelastMsg = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                        chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            thelastMsg = chat.getMessage();
                        }

                }
                switch (thelastMsg){
                    case "default":
                        last_msg.setText("No message");
                        break;
                    default:
                        last_msg.setText(thelastMsg);
                        break;
                }
                thelastMsg ="default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

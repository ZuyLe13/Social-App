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
import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.HolderNotification> {
    private Context context;
    private ArrayList<NotificationModel> notificationList;
    private FirebaseAuth firebaseAuth;
    public NotificationAdapter(Context context, ArrayList<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
        NotificationModel model = notificationList.get(position);
        String noti = model.getNotification();
        String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.ENGLISH);

        holder.nameTV.setText(model.getsName());
        if (Objects.equals(model.getsImage(), "")) {
            holder.avaIV.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(model.getsImage()).into(holder.avaIV);
        }

        holder.notiTV.setText(noti);
        holder.timeTV.setText(sdfDateTime.format(cal.getTime()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FragmentReplacerActivity.class);
                if (noti.contains("comment") || noti.contains("react")) {
                    intent.putExtra("id", model.getpId());
                    intent.putExtra("uid", model.getpUId());
                    intent.putExtra("currentUID", model.getsUid());
                    intent.putExtra("FragmentType", "Comment");
                } else if (noti.contains("friend request")) {
//                    intent.putExtra("uid", model.getsUid());
//                    intent.putExtra("FragmentType", "Profile");
                }
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class HolderNotification extends RecyclerView.ViewHolder{
        ImageView avaIV;
        TextView nameTV, notiTV, timeTV;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            avaIV = itemView.findViewById(R.id.avaIV);
            notiTV = itemView.findViewById(R.id.notiTV);
            timeTV = itemView.findViewById(R.id.timeTV);
        }
    }
}

package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.adapter.MessageAdapter;
import com.example.socialmediaapp.model.ChatModel;
import com.example.socialmediaapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    CircleImageView profileimg;
    TextView username, userStateTV;
    FirebaseUser fuser;
    FirebaseFirestore db;
    DatabaseReference reference;

    ImageButton backBtn, sendBtn;
    EditText message;
    MessageAdapter messageAdapter;
    List<ChatModel> mChat;
    RecyclerView recyclerView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileimg = findViewById(R.id.userAvt);
        username = findViewById(R.id.userNameTV);
        backBtn = findViewById(R.id.backBtn);
        sendBtn = findViewById(R.id.sendMessageBtn);
        message = findViewById(R.id.msgET);
        userStateTV = findViewById(R.id.userStateTV);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mChat = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this, mChat, ""); // Initialize adapter with empty list initially
        recyclerView.setAdapter(messageAdapter); // Set the adapter immediately

        final String uID = getIntent().getStringExtra("uID");

        if (uID != null) {
            loadUserInfoAndReadMessages(uID);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, MessengerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), uID, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "Can not send empty message", Toast.LENGTH_SHORT).show();
                }
                message.setText("");
            }
        });
    }

    private void sendMessage(String sender, String receiver, String msg) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", msg);

        ref.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myid, final String userid, final String imageurl) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    if (chat != null) {
                        if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                            mChat.add(chat);
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged(); // Notify adapter about data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void status(String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (fuser != null) {
            DocumentReference userStatusRef = db.collection("Users").document(fuser.getUid());
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("status", status);

            userStatusRef.update(statusUpdate)
                    .addOnSuccessListener(aVoid -> Log.d("Status Update", "User status updated to " + status))
                    .addOnFailureListener(e -> Log.e("Status Update", "Error updating user status", e));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    private void loadUserInfoAndReadMessages(String uID) {
        db.collection("Users").document(uID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("ChatActivity", "Error while loading user info: " + e.getMessage());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null) {
                        username.setText(user.getName());
                        String imageurl = user.getProfileImg();
                        if (imageurl == null || imageurl.isEmpty()) {
                            profileimg.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(ChatActivity.this)
                                    .load(imageurl)
                                    .into(profileimg);
                        }

                        // Update userStateTV based on the status field from Firestore
                        String status = user.getStatus(); // Assuming UserModel has a getStatus method.
                        if (status != null) {
                            userStateTV.setText(status.equals("online") ? "Online" : "Offline");
                        } else {
                            userStateTV.setText("Offline"); // Default to offline if status is null
                        }

                        // Call readMessages with the image URL
                        readMessages(fuser.getUid(), uID, imageurl);
                    }
                }
            }
        });
    }
}

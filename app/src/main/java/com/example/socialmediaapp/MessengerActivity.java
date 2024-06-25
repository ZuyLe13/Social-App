package com.example.socialmediaapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.adapter.UserChatAdapter;
import com.example.socialmediaapp.model.ChatModel;
import com.example.socialmediaapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class MessengerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserChatAdapter userChatAdapter;
    private List<UserModel> mUsers;
    private EditText searchET;
    private ImageButton backBtn, settingBtn;
    FirebaseUser fuser;
    DatabaseReference chatRef;
    DatabaseReference activeRef;
    Set<String> usersSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchET = findViewById(R.id.searchET);
        backBtn = findViewById(R.id.backBtn);
        settingBtn = findViewById(R.id.settingBtn);

        mUsers = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(this, mUsers, false);
        recyclerView.setAdapter(userChatAdapter);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        usersSet = new HashSet<>();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        activeRef = FirebaseDatabase.getInstance().getReference("UserActive").child(fuser.getUid());


        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersSet.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatModel chat = snapshot.getValue(ChatModel.class);

                    if (chat != null) {
                        if (chat.getSender().equals(fuser.getUid())) {
                            usersSet.add(chat.getReceiver());
                        }
                        if (chat.getReceiver().equals(fuser.getUid())) {
                            usersSet.add(chat.getSender());
                        }
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void readChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(MessengerActivity.this, "Error while loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    mUsers.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && usersSet.contains(user.getuID())) {
                            mUsers.add(user);
                        }
                    }
                    userChatAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    private void active(String status) {
        if (fuser != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("active", status);
            hashMap.put("uID", fuser.getUid()); // Thêm uID vào bản ghi trong Realtime Database

            activeRef.setValue(hashMap);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        active("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        active("offline");
    }
//    private void readUsers() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference usersRef = db.collection("Users");
//
//        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Toast.makeText(MessengerActivity.this, "Error while loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (queryDocumentSnapshots != null) {
//                    mUsers.clear();
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        UserModel user = document.toObject(UserModel.class);
//                        if (user != null && !user.getuID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                            mUsers.add(user);
//                        }
//                    }
//                    for (UserModel user : mUsers) {
//                        Log.d("MessengerActivity", "User: " + user.getName() + ", UID: " + user.getuID());
//                    }
//                    userChatAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//    }
}

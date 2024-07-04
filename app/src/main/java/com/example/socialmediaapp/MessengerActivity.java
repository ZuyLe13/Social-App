package com.example.socialmediaapp;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.socialmediaapp.Notifications.Token;
import com.example.socialmediaapp.adapter.UserChatAdapter;
import com.example.socialmediaapp.model.ChatModel;
import com.example.socialmediaapp.model.Chatlist;
import com.example.socialmediaapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class MessengerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserChatAdapter userChatAdapter;
    private List<UserModel> mUsers;
    private EditText searchET;
    private ImageButton backBtn, settingBtn;
    FirebaseUser fuser;
    DatabaseReference chatRef;
    Set<String> usersSet;
    private List<Chatlist> usersList;

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
        userChatAdapter = new UserChatAdapter(this, mUsers, true);
        recyclerView.setAdapter(userChatAdapter);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
//        usersList = new ArrayList<>();
//        chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
//        chatRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                usersList.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
//                    usersList.add(chatlist);
//                }
//                chatList();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        usersSet = new HashSet<>();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
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

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // Call updateToken to update the token on activity start
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//
//                    // Get new FCM registration token
//                    String token = task.getResult();
//                    updateToken(token);
//                });
    }

    private void chatList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mUsers = new ArrayList<>();

        for (Chatlist chatlist : usersList) {
            DocumentReference docRef = db.collection("Users").document(chatlist.getId());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            mUsers.add(user);
                        }
                        userChatAdapter.notifyDataSetChanged(); // Update your adapter once data is added
                    } else {
                        Log.d("MessengerActivity", "No such user");
                    }
                } else {
                    Log.d("MessengerActivity", "get failed with ", task.getException());
                }
            });
        }
        recyclerView.setAdapter(userChatAdapter);
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

    private void searchUsers(String s) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("Users").whereGreaterThanOrEqualTo("search", s)
                .whereLessThanOrEqualTo("search", s + "\uf8ff");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("SearchUsers", "Error while searching users: " + e.getMessage());
                    return;
                }

                mUsers.clear();
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = snapshot.toObject(UserModel.class);
                        if (user != null && !user.getuID().equals(fuser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                    userChatAdapter.notifyDataSetChanged(); // Notify the adapter
                }
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
//    private void updateToken (String token){
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token token1 = new Token(token);
//        reference.child(fuser.getUid()).setValue(token1);
//    }
}

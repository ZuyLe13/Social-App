package com.example.socialmediaapp;

import static com.google.common.io.Files.getFileExtension;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.adapter.MessageAdapter;
import com.example.socialmediaapp.model.ChatModel;
import com.example.socialmediaapp.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 1;
    CircleImageView profileimg;
    TextView username, userStateTV;
    FirebaseUser fuser;
    FirebaseFirestore db;
    DatabaseReference reference;

    ImageButton backBtn, sendBtn, sendImageBtn;
    EditText message;
    MessageAdapter messageAdapter;
    List<ChatModel> mChat;
    RecyclerView recyclerView;
    Intent intent;
    ValueEventListener seenListener;
    Uri imageUri;
    String uID;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupRecyclerView();
        initFirebase();

        uID = getIntent().getStringExtra("uID");

        if (uID != null) {
            loadUserInfoAndReadMessages(uID);
            seenMessage(uID);
            registerResult();
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
                sendMessage();
            }
        });
        
        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void initFirebase() {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mChat = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this, mChat, ""); // Initialize adapter with empty list initially
        recyclerView.setAdapter(messageAdapter); // Set the adapter immediately
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        profileimg = findViewById(R.id.userAvt);
        username = findViewById(R.id.userNameTV);
        backBtn = findViewById(R.id.backBtn);
        sendBtn = findViewById(R.id.sendMessageBtn);
        message = findViewById(R.id.msgET);
        userStateTV = findViewById(R.id.userStateTV);
        sendImageBtn = findViewById(R.id.sendImageBtn);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, IMAGE_REQUEST_CODE);
//        resultLauncher.launch(intent);
    }
    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            uploadImage();
//                            try {
//                                sendImgMessage(imageUri);
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
                        }
                    }
                }
        );
    }

    private void sendImgMessage(Uri imageUri) throws IOException {
        String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", uID);
                            hashMap.put("message", downloadUri);
                            hashMap.put("isseen", false);
                            hashMap.put("isImage", true);
                        }
                    }
                });
    }

    private void sendMessage() {
        String msg = message.getText().toString();
        if (!msg.equals("")) {
            sendMessageToFirebase(fuser.getUid(), uID, msg, false);
        } else {
            Toast.makeText(ChatActivity.this, "Can not send empty message", Toast.LENGTH_SHORT).show();
        }
        message.setText("");
    }

    private void sendMessageToFirebase(String sender, String receiver, String msg, boolean isImage) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", msg);
        hashMap.put("isseen", false);
        hashMap.put("isImage", isImage);  // Đảm bảo isImage được đặt đúng

        ref.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("sendMessage", "Message sent successfully. isImage: " + isImage);
                } else {
                    Log.d("sendMessage", "Message sending failed.", task.getException());
                }
            }
        });
    }


    private void uploadImage() {
        if (imageUri != null) {
            final StorageReference fileReference = FirebaseStorage.getInstance().getReference("ChatImages")
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.loading_dialog);
            dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_bg, null));
            dialog.setCancelable(false);
            dialog.show();

            fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dialog.dismiss();
                                String imageUrl = uri.toString();
                                sendMessageToFirebase(fuser.getUid(), uID, imageUrl, true);  // Set isImage to true here
                                Log.d("uploadImage", "yes");

                            }
                        });
                    } else {
                        dialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        String extension;
        // Using content resolver to get the MIME type
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        extension = mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        return extension;
    }


//    private String getFileExtension(Uri uri) {
//        return getContentResolver().getType(uri).split("/")[1];
//    }

    private void seenMessage (String userid){
//        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String msg, boolean isImage) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", msg);
        hashMap.put("isseen", false);
        hashMap.put("isImage", isImage);

        ref.child("Chats").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("sendMessage", "Message sent successfully. isImage: " + isImage);
                } else {
                    Log.d("sendMessage", "Message sending failed.", task.getException());
                }
            }
        });

        // add user to chat fragment
//        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(fuser.getUid())
//                .child(receiver);
//
//        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()){
//                    chatRef.child("id").setValue(receiver);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private void readMessages(final String myid, final String userid, final String imageurl) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    Log.d("ChatActivity", "Read message: " + chat.getMessage() + ", isImage: " + chat.getIsImage());

                    if (chat != null) {
                        Log.d("ChatActivity", "Read message: " + chat.getMessage() + ", isImage: " + chat.getIsImage() +
                                ", isseen: " + chat.isIsseen() +
                                ", sender: " + chat.getSender() +
                                ", receiver: " + chat.getReceiver());
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
        userStateTV.setText("Online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        userStateTV.setText("Offline"); // Default to offline if status is null

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
                            Glide.with(getApplicationContext())
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

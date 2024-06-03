package com.example.socialmediaapp.fragments;

import static com.example.socialmediaapp.fragments.Profile.collectionFixSize;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.CollectionAdapter;
import com.example.socialmediaapp.adapter.CommentAdapter;
import com.example.socialmediaapp.model.CollectionModel;
import com.example.socialmediaapp.model.CommentModel;
import com.example.socialmediaapp.model.HomeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comment extends Fragment {


    private EditText commentET;
    private ImageButton commentSendBtn;
    private RecyclerView commentRV;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;
    private FirebaseUser user;
    private String id, uid, currentUID;
    private CollectionReference referenceAll, reference;
    private int fixSize = 0;

    private CircleImageView profileImage;
    private TextView userNameTV, timeStampTV, reactCountTV, descriptionTV;
    private ImageView imageView;
    private ImageButton reactBtn, shareBtn, AtoCBtn;
    private List<String> reacts;
    public Comment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Cấu hình bàn phím mềm
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init(view);
        loadDataFromFireStore();
    }

    private void init(View view){
        commentET = view.findViewById(R.id.commentDetailET);

        commentET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            }
        });

        commentSendBtn = view.findViewById(R.id.commentDetailSendBtn);
        commentRV = view.findViewById(R.id.commentDetailRV);


        profileImage = view.findViewById(R.id.userAvt);
        imageView = view.findViewById(R.id.postImageView);
        userNameTV = view.findViewById(R.id.userName);
        timeStampTV = view.findViewById(R.id.timeStampTextView);
        reactCountTV = view.findViewById(R.id.reactCountTextView);
        reactBtn = view.findViewById(R.id.reactBtn);
        descriptionTV = view.findViewById(R.id.desciptionTextView);
        shareBtn = view.findViewById(R.id.shareBtn);
        AtoCBtn = view.findViewById(R.id.AtoCBtn);

        commentList = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() == null) return;
        id = getArguments().getString("id");
        uid = getArguments().getString("uid");
        currentUID = getArguments().getString("currentUID");

        referenceAll = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Post Images");
        reference = referenceAll.document(id).collection("Comments");


        referenceAll.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot res = task.getResult();
                    if (res.exists()){
                        HomeModel model = res.toObject(HomeModel.class);
                        userNameTV.setText(model.getName());
                        timeStampTV.setText(model.getTimeStamp().toString());
                        descriptionTV.setText(model.getDescription());

                        reacts = model.getReacts();
                        if (reacts.size() == 0) {
                            reactCountTV.setVisibility(View.GONE);
                        } else {
                            reactCountTV.setVisibility(View.VISIBLE);
                            reactCountTV.setText(String.valueOf(reacts.size()));
                        }
                        if (reacts.contains(currentUID)){
                            reactBtn.setImageResource(R.drawable.ic_heart_fill);
                        } else {
                            reactBtn.setImageResource(R.drawable.ic_heart);
                        }

                        Glide.with(view.getContext().getApplicationContext())
                                .load(model.getProfileImage())
                                .placeholder(R.drawable.ic_avt)
                                .timeout(6500)
                                .into(profileImage);
                        Glide.with(view.getContext().getApplicationContext())
                                .load(model.getImageUrl())
                                .placeholder(R.drawable.ic_find_picture)
                                .timeout(7000)
                                .into(imageView);

                        setClickListener(
                                model.getId(),
                                model.getName(),
                                model.getUid(),
                                currentUID,
                                model.getReacts(),
                                reacts.contains(currentUID) ? 1 : 0,
                                model.getImageUrl(),
                                model.getTimeStamp()
                        );
                    }
                }
            }
        });

        commentRV.setLayoutManager(new LinearLayoutManager(view.getContext()));
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentRV.setAdapter(commentAdapter);
    }

    private void loadDataFromFireStore(){
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null || value == null) return;

                commentList.clear();
                for (QueryDocumentSnapshot snapshot: value){
                    CommentModel model = snapshot.toObject(CommentModel.class);
                    FirebaseFirestore.getInstance().collection("Users").document(model.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot commentUserValue = task.getResult();
                                if (commentUserValue.exists()) {
                                    String profileImg = commentUserValue.getString("profileImg");
                                    String name = commentUserValue.getString("name");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("avt", profileImg);
                                    map.put("name", name);
                                    reference.document(model.getId()).update(map);
                                    model.setAvt(profileImg);
                                    model.setName(name);
                                }
                            }
                        }
                    });

                    commentList.add(model);
                }

                commentAdapter.notifyDataSetChanged();
            }
        });

        fixSize = commentList.size();
    }

    private void setClickListener(String id, String name, String uID, String currentUID, List<String> reacts, int isChecked, String imageURL, Date timestamp){

        commentSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentET.getText().toString();
                if (comment.isEmpty() || comment.equals(" ")){
                    Toast.makeText(getContext(), "Please enter a valid comment", Toast.LENGTH_SHORT).show();
                }

                String commentID = reference.document().getId();

                Map<String, Object> map = new HashMap<>();
                map.put("uid", currentUID);
                map.put("postid", id);
                map.put("id", commentID);
                map.put("comment", comment);
                map.put("timestamp", new Timestamp(System.currentTimeMillis() / 1000, 0));
                map.put("name", user.getDisplayName().toString());
                map.put("avt", user.getPhotoUrl().toString());

                reference.document(commentID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            commentET.setText("");
                            referenceAll.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()){
                                            int commentCount = document.getLong("commentCount").intValue();
                                            Map<String, Object> map2 = new HashMap<>();
                                            map2.put("commentCount", commentCount + 1);
                                            referenceAll.document(id).update(map2);
                                            fixSize += 1;
                                        }
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(getContext(), "Failed to comment, error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                while (commentList.size() != fixSize){
                    commentList.remove(0);
                }
            }
        });

        reactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uID)
                        .collection("Post Images")
                        .document(id);

                if (reacts.contains(currentUID)){
                    reacts.remove(currentUID);
                    reactBtn.setImageResource(R.drawable.ic_heart);
                } else {
                    reacts.add(currentUID);
                    reactBtn.setImageResource(R.drawable.ic_heart_fill);
                }
                if (reacts.size() == 0) {
                    reactCountTV.setVisibility(View.GONE);
                } else {
                    reactCountTV.setVisibility(View.VISIBLE);
                    reactCountTV.setText(String.valueOf(reacts.size()));
                }

                Map<String, Object> map = new HashMap<>();
                map.put("reacts", reacts);
                documentReference.update(map);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, imageURL);
                intent.setType("text/*");
                view.getContext().startActivity(Intent.createChooser(intent, "Share link ..."));
            }
        });

        AtoCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_add_to_collection, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(dialogView)
                        .setTitle("Saving This Post");

                final AlertDialog dialog = builder.create();

                Button addPostBtn = dialogView.findViewById(R.id.btnNewCollection);
                RecyclerView collectionRV = dialogView.findViewById(R.id.recyclerView);
                List<CollectionModel> collectionList = new ArrayList<>();
                CollectionAdapter collectionAdapter = new CollectionAdapter(collectionList, (Activity) view.getContext(), currentUID, "Post", id, imageURL, name, timestamp, uID);
                collectionRV.setLayoutManager(new LinearLayoutManager(view.getContext()));
                collectionRV.setAdapter(collectionAdapter);

                FirebaseFirestore.getInstance()
                        .collection("Users").document(currentUID)
                        .collection("Collections")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (error != null){
                                    Log.e("Error: ", error.getMessage());
                                    return;
                                }

                                if (value == null) return;

                                collectionList.clear();
                                for (QueryDocumentSnapshot snapshot : value) {
                                    CollectionModel model = snapshot.toObject(CollectionModel.class);
                                    collectionList.add(model);
                                }
                                collectionAdapter.notifyDataSetChanged();
                            }
                        });
                collectionFixSize = collectionList.size();


                addPostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater inflater = LayoutInflater.from(view.getContext());
                        View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setView(dialogView)
                                .setTitle("Add New Collection")
                                .setPositiveButton("Create", null)
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        final AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText ACNameET = dialogView.findViewById(R.id.dialogACNameET);
                                EditText ACDescriptionET = dialogView.findViewById(R.id.dialogACDescriptionET);
                                String name = ACNameET.getText().toString().trim();
                                String description = ACDescriptionET.getText().toString().trim();

                                if (name.isEmpty()) {
                                    ACNameET.setError("Name is required");
                                    return;
                                }

                                createNewCollection(view, name, description, currentUID, collectionList);
                                dialog.dismiss();
                            }
                        });

                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(dialogView.findViewById(R.id.dialogACNameET), InputMethodManager.SHOW_IMPLICIT);
                            }
                        });
                    }
                });

                dialog.show();
            }
        });

    }



    private void createNewCollection(View view, String name, String description, String currentUID, List<CollectionModel> collectionList) {
        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUID)
                .collection("Collections");
        String collectionID = reference.document().getId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() / 1000, 0);
        EditText ACNameET = view.findViewById(R.id.dialogACNameET);
        EditText ACDescriptionET = view.findViewById(R.id.dialogACDescriptionET);

        Map<String, Object> map = new HashMap<>();
        map.put("id", collectionID);
        map.put("name", name);
        map.put("description", description);
        map.put("timestamp", timestamp);
        map.put("postCount", 0);

        reference.document(collectionID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    DocumentReference document = FirebaseFirestore.getInstance().collection("Users").document(currentUID);
                    document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().exists()){
                                    int collectionCount = task.getResult().getLong("collectionCount").intValue();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("collectionCount", collectionCount + 1);
                                    document.update(map);
                                    collectionFixSize += 1;
                                }
                            }
                        }
                    });
                }
            }
        });
        while (collectionList.size() != collectionFixSize){
            collectionList.remove(0);
        }
    }
}
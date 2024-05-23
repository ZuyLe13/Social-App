package com.example.socialmediaapp.fragments;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.CommentAdapter;
import com.example.socialmediaapp.model.CommentModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment extends Fragment {


    private EditText commentET;
    private ImageButton commentSendBtn;
    private RecyclerView commentRV;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList;
    private FirebaseUser user;
    private String id, uid;
    private CollectionReference referenceAll, reference;
    private int fixSize = 0;
    public Comment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        init(view);
        loadDataFromFireStore();
        setClickListener();
    }

    private void init(View view){
        commentET = view.findViewById(R.id.commentDetailET);
        commentSendBtn = view.findViewById(R.id.commentDetailSendBtn);
        commentRV = view.findViewById(R.id.commentDetailRV);

        commentList = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() == null) return;
        id = getArguments().getString("id");
        uid = getArguments().getString("uid");

        referenceAll = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .collection("Post Images");
        reference = referenceAll.document(id).collection("Comments");

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

                for (QueryDocumentSnapshot snapshot: value){
                    CommentModel model = snapshot.toObject(CommentModel.class);
                    commentList.add(model);
                }

                commentAdapter.notifyDataSetChanged();
            }
        });

        fixSize = commentList.size();
    }

    private void setClickListener(){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(name)
                .setPhotoUri(Uri.parse("https://toppng.com/uploads/preview/person-vector-11551054765wbvzeoxz2c.png"))
                .build();
        user.updateProfile(profileUpdates);

        commentSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentET.getText().toString();
                if (comment.isEmpty() || comment.equals(" ")){
                    Toast.makeText(getContext(), "Please enter a valid comment", Toast.LENGTH_SHORT).show();
                }

                String commentID = reference.document().getId();

                Map<String, Object> map = new HashMap<>();
                map.put("uid", uid);
                map.put("postid", id);
                map.put("id", commentID);
                map.put("comment", comment);
                map.put("timestamp", new Timestamp(System.currentTimeMillis() / 1000, 0));
                map.put("name", uid);
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
    }
}
package com.example.socialmediaapp.adapter;

import static com.example.socialmediaapp.fragments.Collection.CPFixSize;
import static com.example.socialmediaapp.fragments.Profile.collectionFixSize;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.CollectionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionHolder> {

    private List<CollectionModel> collectionList;
    private Activity context;
    private String currentUID, usage, postToAddID, postImg, uname, postUID;
    @ServerTimestamp
    private Date postTimestamp;

    public CollectionAdapter(List<CollectionModel> collectionList, Activity context, String currentUID, String usage, String postToAddID, String postImg, String uname, Date postTimestamp, String postUID){
        this.collectionList = collectionList;
        this.context = context;
        this.currentUID = currentUID;
        this.usage = usage;
        this.postToAddID = postToAddID;
        this.postImg = postImg;
        this.uname = uname;
        this.postTimestamp = postTimestamp;
        this.postUID = postUID;
    }

    @NonNull
    @Override
    public CollectionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent,false);
        return new CollectionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionHolder holder, int position) {
        holder.nameTV.setText(collectionList.get(position).getName());
        holder.descTV.setText(collectionList.get(position).getDescription());
        holder.postCountTV.setText(String.valueOf(collectionList.get(position).getPostCount()));
        holder.setClickListener(collectionList.get(position).getId(), collectionList.get(position).getName(), postToAddID, postImg, uname, postTimestamp, postUID);

        if (usage.equals("Profile")){
            holder.detailBtn.setVisibility(View.VISIBLE);
            holder.addPostBtn.setVisibility(View.GONE);
        } else if (usage.equals("Post")){
            holder.detailBtn.setVisibility(View.GONE);
            holder.addPostBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    public class CollectionHolder extends RecyclerView.ViewHolder {
        private TextView nameTV, descTV, postCountTV;
        private ImageButton detailBtn, deleteBtn, addPostBtn;

        public CollectionHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.collectionNameTV);
            descTV = itemView.findViewById(R.id.collectionDescTV);
            postCountTV = itemView.findViewById(R.id.collectionPostCountTV);
            detailBtn = itemView.findViewById(R.id.collectionDetailBtn);
            deleteBtn = itemView.findViewById(R.id.collectionDeleteBtn);
            addPostBtn = itemView.findViewById(R.id.collectionAddPostBtn);
        }

        public void setClickListener(String collectionID, String collectionName, String postToAddID, String postImg, String uname, Date postTimestamp, String postUID){
            // id tu tao, postImg, uname, postTimestamp, postID, uid (postUID) lay tu adapter
            detailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, FragmentReplacerActivity.class);
                    intent.putExtra("collectionID", collectionID);
                    intent.putExtra("collectionName", collectionName);
                    intent.putExtra("collectionUID", currentUID);
                    intent.putExtra("FragmentType", "Collection");

                    context.startActivity(intent);
                    while (collectionList.size() != collectionFixSize){
                        collectionList.remove(0);
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users")
                            .document(currentUID)
                            .collection("Collections")
                            .document(collectionID);

                    docRef.collection("CollectionItems").get().addOnCompleteListener(subTask -> {
                        if (subTask.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : subTask.getResult()) {
                                snapshot.getReference().delete().addOnCompleteListener(deleteSubTask -> {
                                    if (!deleteSubTask.isSuccessful()) {
                                        Log.w("Firestore", "Error deleting Collection Items fail: ", deleteSubTask.getException());
                                    }
                                });
                            }

                            docRef.delete().addOnCompleteListener(deleteTask -> {
                                if (!deleteTask.isSuccessful()) {
                                    Log.w("Firestore", "Error deleting Collection fail: ", deleteTask.getException());
                                } else {
                                    DocumentReference document = FirebaseFirestore.getInstance().collection("Users").document(currentUID);
                                    document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                if (task.getResult().exists()){
                                                    int collectionCount = task.getResult().getLong("collectionCount").intValue();
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("collectionCount", collectionCount - 1);
                                                    document.update(map);
                                                    collectionFixSize -= 1;
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            Log.w("Firestore", "Error getting Collection Items fail: ", subTask.getException());
                        }
                    });

                    Log.d("TEST !!!", "fix size: " + collectionFixSize);
                    Log.d("TEST !!!", "list size: " + collectionList.size());
                    while (collectionList.size() != collectionFixSize){
                        collectionList.remove(0);
                    }
                }
            });

            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CollectionReference reference = FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(currentUID)
                            .collection("Collections")
                            .document(collectionID)
                            .collection("CollectionItems");
                    String collectionItemID = reference.document().getId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", collectionItemID);
                    map.put("postImg", postImg);
                    map.put("postid", postToAddID);
                    map.put("uid", postUID);
                    map.put("uname", uname);
                    map.put("postTimestamp", postTimestamp);

                    reference.document(collectionItemID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(view.getContext(), "Add to collection successfully", Toast.LENGTH_SHORT).show();
                                DocumentReference document = FirebaseFirestore.getInstance().collection("Users").document(currentUID).collection("Collections").document(collectionID);
                                document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            if (task.getResult().exists()){
                                                int postCount = task.getResult().getLong("postCount").intValue();
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("postCount", postCount + 1);
                                                document.update(map);
                                                CPFixSize += 1;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }
}

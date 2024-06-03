package com.example.socialmediaapp.adapter;

import static com.example.socialmediaapp.fragments.Collection.CPFixSize;
import static com.example.socialmediaapp.fragments.Profile.collectionFixSize;
import static com.example.socialmediaapp.utils.TimeUtils.getTimeAgo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.CollectionPostModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionPostAdapter extends RecyclerView.Adapter<CollectionPostAdapter.CollectionPostHolder> {
    private List<CollectionPostModel> postList;
    private Activity context;
    private String currentUID, collectionID;

    public CollectionPostAdapter(List<CollectionPostModel> postList, Activity context, String currentUID, String collectionID){
        this.postList = postList;
        this.context = context;
        this.currentUID = currentUID;
        this.collectionID = collectionID;
    }

    @NonNull
    @Override
    public CollectionPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_post, parent,false);
        return new CollectionPostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionPostHolder holder, int position) {
        holder.nameTV.setText(postList.get(position).getUname());
        Glide.with(context.getApplicationContext())
                .load(postList.get(position).getPostImg())
                .placeholder(R.drawable.ic_find_picture)
                .timeout(6500)
                .into(holder.imageIV);
        holder.timeTV.setText(getTimeAgo(postList.get(position).getPostTimestamp()));
        holder.setClickListener(
                collectionID,
                postList.get(position).getId(),
                postList.get(position).getPostid(),
                postList.get(position).getUid(),
                currentUID
                );
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class CollectionPostHolder extends RecyclerView.ViewHolder{
        private TextView nameTV, timeTV;
        private ImageView imageIV;
        private ImageButton detailBtn, deleteBtn;
        public CollectionPostHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.CPNameTV);
            timeTV = itemView.findViewById(R.id.CPTimeTV);
            imageIV = itemView.findViewById(R.id.CPImageIV);
            detailBtn = itemView.findViewById(R.id.CPDetailBtn);
            deleteBtn = itemView.findViewById(R.id.CPDeleteBtn);
        }

        public void setClickListener(String CollectionID, String collectionItemID, String postID, String postUID, String currentUID){
            detailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, FragmentReplacerActivity.class);
                    intent.putExtra("id", postID);
                    intent.putExtra("uid", postUID);
                    intent.putExtra("currentUID", currentUID);
                    intent.putExtra("FragmentType", "Comment");

                    context.startActivity(intent);
                    while (postList.size() != CPFixSize){
                        postList.remove(0);
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DocumentReference documentRef = FirebaseFirestore.getInstance().collection("Users")
                            .document(currentUID)
                            .collection("Collections")
                            .document(collectionID)
                            .collection("CollectionItems")
                            .document(collectionItemID);

                    documentRef.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Log.d("Firestore", "Collection item successfully deleted!");

                            DocumentReference document = FirebaseFirestore.getInstance().collection("Users").document(currentUID).collection("Collections").document(collectionID);
                            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (task.getResult().exists()){
                                            int postCount = task.getResult().getLong("postCount").intValue();
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("postCount", postCount - 1);
                                            document.update(map);
                                            CPFixSize -= 1;
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.w("Firestore", "Error deleting Collection Item: ", deleteTask.getException());
                        }
                    });

                    while (postList.size() != CPFixSize){
                        postList.remove(0);
                    }
                }
            });
        }

    }
}

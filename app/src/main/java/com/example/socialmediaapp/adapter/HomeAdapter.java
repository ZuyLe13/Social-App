package com.example.socialmediaapp.adapter;

import static com.example.socialmediaapp.fragments.Home.fixSize;
import static com.example.socialmediaapp.fragments.Profile.collectionFixSize;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.fragments.Home;
import com.example.socialmediaapp.model.CollectionModel;
import com.example.socialmediaapp.model.HomeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private List<HomeModel> list;
    private Activity context;
    private OnPressed onPressed;


    public HomeAdapter(List<HomeModel> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);

        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        holder.userNameTV.setText(list.get(position).getName());
        holder.timeStampTV.setText("" + list.get(position).getTimeStamp());

        List<String> reacts = list.get(position).getReacts();
        if (reacts.size() == 0) {
            holder.reactCountTV.setVisibility(View.GONE);
        } else {
            holder.reactCountTV.setVisibility(View.VISIBLE);
            holder.reactCountTV.setText(String.valueOf(reacts.size()));
        }

        if (reacts.contains(user.getUid())){
            holder.reactBtn.setImageResource(R.drawable.ic_heart_fill);
        } else {
            holder.reactBtn.setImageResource(R.drawable.ic_heart);
        }

        int commentCount =list.get(position).getCommentCount();
        holder.commentCountTV.setText(String.valueOf(commentCount));

        if (commentCount == 0) holder.commentTV.setVisibility(View.GONE);
        else {
            holder.commentTV.setVisibility(View.VISIBLE);
            if (commentCount == 1) holder.commentTV.setText("See comment");
            else holder.commentTV.setText("See all " + String.valueOf(commentCount) + " comments");
        }

        holder.descriptionTV.setText(list.get(position).getDescription());

        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.ic_avt)
                .timeout(6500)
                .into(holder.profileImage);

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        holder.clickListener(
                position,
                list.get(position).getId(),
                list.get(position).getName(),
                list.get(position).getUid(),
                user.getUid(),
                list.get(position).getReacts(),
                reacts.contains(user.getUid()) ? 1 : 0,
                list.get(position).getImageUrl(),
                list.get(position).getTimeStamp()
        );

//        list.get(position).getSnapshot().getReference().collection("Comments")
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null || value == null) return;
//
//                        for (QueryDocumentSnapshot snapshot : value){
//
//                        }
//                    }
//                });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HomeHolder extends RecyclerView.ViewHolder{

        private CircleImageView profileImage;
        private TextView userNameTV, timeStampTV, commentCountTV, reactCountTV, descriptionTV, commentTV;
        private ImageView imageView;
        private ImageButton reactBtn, commentBtn, shareBtn, commentSendBtn, AtoCBtn;
        private EditText commentET;
        private LinearLayout commentLL;
        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.userAvt);
            imageView = itemView.findViewById(R.id.postImageView);
            userNameTV = itemView.findViewById(R.id.userName);
            timeStampTV = itemView.findViewById(R.id.timeStampTextView);
            reactCountTV = itemView.findViewById(R.id.reactCountTextView);
            commentCountTV = itemView.findViewById(R.id.commentCountTextView);
            reactBtn = itemView.findViewById(R.id.reactBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTV = itemView.findViewById(R.id.desciptionTextView);
//            commentET = itemView.findViewById(R.id.commentET);
//            commentSendBtn = itemView.findViewById(R.id.commentSendBtn);
//            commentLL = itemView.findViewById(R.id.commentLL);
            commentTV = itemView.findViewById(R.id.commentTV);
            AtoCBtn = itemView.findViewById(R.id.AtoCBtn);
        }

        public void clickListener(int position, String id, String name, String uID, String currentUID, List<String> reacts, int isChecked, String imageURL, Date timestamp) {
            reactBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    onPressed.onReacted(position, id, uID, reacts, isChecked);
                }
            });

            commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (commentLL.getVisibility() == View.GONE){
//                        commentLL.setVisibility(View.VISIBLE);
//                    } else {
//                        commentLL.setVisibility(View.GONE);
//                    }

                    Intent intent = new Intent(context, FragmentReplacerActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("uid", uID);
                    intent.putExtra("currentUID", currentUID);
                    intent.putExtra("FragmentType", "Comment");

                    context.startActivity(intent);
                    while (list.size() != fixSize){
                        list.remove(0);
                    }
                }
            });

//            commentSendBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String commment = commentET.getText().toString();
//                    onPressed.onCommented(position, id, uID, commment, commentLL, commentET);
//                }
//            });

            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, imageURL);
                    intent.setType("text/*");
                    context.startActivity(Intent.createChooser(intent, "Share link ..."));
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


    public interface OnPressed{
        void onReacted (int position, String id, String uID, List<String> reacts, int isChecked);
//        void onCommented(int position, String id, String uID, String comment, LinearLayout commentLL, EditText commentET);
    }
    public void OnPressed (OnPressed onPressed){
        this.onPressed = onPressed;
    }

}

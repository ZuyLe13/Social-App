package com.example.socialmediaapp.fragments;

import static android.app.Activity.RESULT_OK;

import static com.example.socialmediaapp.MainActivity.currentUid;
import static com.example.socialmediaapp.MainActivity.isSearching;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.CollectionAdapter;
import com.example.socialmediaapp.model.CollectionModel;
import com.example.socialmediaapp.model.HomeModel;
import com.example.socialmediaapp.model.PostModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment{

    private TextView nameTV, toolbarNameTV, statusTV, followingCountTV, follwersCountTV;
    private CircleImageView profileImage;
    private Button followBtn, collectionAddBtn, shotsCountBtn, collectionsCountBtn;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private ImageButton editProfileBtn, settingBtn;
    private LinearLayout countLayout;
    private Boolean isMyProfile = true;
    FirestoreRecyclerAdapter<PostModel, PostHolder> adapter;
    private CollectionAdapter collectionAdapter;
    private List<CollectionModel> collectionList;
    public static int collectionFixSize = 0;
    private EditText ACNameET, ACDescriptionET;
    private Context context;
    private Boolean isCreating = true;

    String uid, PREF_URL, PREF_DIRECTORY, PREF_STORED;
    int PREF_NAME;
    public Profile(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        init(view);
        if (isSearching){
            uid = currentUid;
            isMyProfile = false;
        } else {
            uid = user.getUid();
            isMyProfile = true;
        }

        if (isMyProfile){
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
            editProfileBtn.setVisibility(View.VISIBLE);
        } else {
            followBtn.setVisibility(View.VISIBLE);
            countLayout.setVisibility(View.GONE);
            editProfileBtn.setVisibility(View.GONE);
            settingBtn.setVisibility(View.GONE);
            shotsCountBtn.setVisibility(View.GONE);
            collectionsCountBtn.setVisibility(View.GONE);
        }

        loadBasicData();

        recyclerView.setHasFixedSize(true);


        loadPostData(view);

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(), Profile.this);
            }
        });

        shotsCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectionAddBtn.setVisibility(View.GONE);
                loadPostData(view);
            }
        });

        collectionsCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectionAddBtn.setVisibility(View.VISIBLE);
                loadCollectionData(view);
            }
        });

        collectionAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        ACNameET = dialogView.findViewById(R.id.dialogACNameET);
                        ACDescriptionET = dialogView.findViewById(R.id.dialogACDescriptionET);
                        String name = ACNameET.getText().toString().trim();
                        String description = ACDescriptionET.getText().toString().trim();

                        if (name.isEmpty()) {
                            ACNameET.setError("Name is required");
                            return;
                        }

                        createNewCollection(name, description);
                        dialog.dismiss();
                    }
                });

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(dialogView.findViewById(R.id.dialogACNameET), InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        this.context = view.getContext();

        this.nameTV = view.findViewById(R.id.nameTv);
        this.statusTV = view.findViewById(R.id.statusTV);
        this.toolbarNameTV = view.findViewById(R.id.toolbarNameTV);
        this.followingCountTV = view.findViewById(R.id.followingCountTv);
        this.follwersCountTV = view.findViewById(R.id.followersCountTv);
        this.profileImage = view.findViewById(R.id.profileImage);
        this.followBtn = view.findViewById(R.id.followBtn);
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.countLayout = view.findViewById(R.id.countLayout);
        this.editProfileBtn = view.findViewById(R.id.edit_profileImage);
        this.settingBtn = view.findViewById(R.id.settingBtn);
        this.shotsCountBtn = view.findViewById(R.id.shotsCountBtn);
        this.collectionsCountBtn = view.findViewById(R.id.collectionsCountBtn);
        this.collectionAddBtn = view.findViewById(R.id.collectionAddBtn);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void loadBasicData(){
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(uid);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){

                    return;
                }
                if (value.exists()){
                    String name = value.getString("name");
                    String status = value.getString("status");
                    List<String> following = (List<String>)value.get("following");
                    List<String> followers = (List<String>)value.get("followers");
                    String profileURL = value.getString("profileImg");
                    int collectionCount = value.getLong("collectionCount").intValue();

                    nameTV.setText(name);
                    toolbarNameTV.setText(name);
                    statusTV.setText(status);
                    followingCountTV.setText(String.valueOf(following.size()));
                    follwersCountTV.setText(String.valueOf(followers.size()));
                    collectionsCountBtn.setText(String.valueOf(collectionCount) + " Collections");

                    Glide.with(context.getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.ic_avt)
                            .circleCrop()
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
//                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
//                                    storeProfileImage(bitmap, profileURL);
//                                    return false;
//                                }
//                            })
                            .timeout(6500)
                            .into(profileImage);


                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse(profileURL))
                            .build();
                    user.updateProfile(profileUpdates);
                }
            }
        });
    }

    public void loadPostData(View view){

        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(uid);
        Query query = reference.collection("Post Images");
        FirestoreRecyclerOptions<PostModel> options = new FirestoreRecyclerOptions.Builder<PostModel>()
                .setQuery(query, PostModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PostModel, PostHolder>(options) {
            @NonNull
            @Override
            public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_post_item, parent, false);
                return new PostHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostHolder postHolder, int i, @NonNull PostModel postModel) {
                Glide.with(postHolder.itemView.getContext().getApplicationContext())
                        .load(postModel.getImageUrl())
                        .timeout(6500)
                        .into(postHolder.profilePostImageView);

            }
        };

        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
        recyclerView.setAdapter(adapter);
        shotsCountBtn.setText(String.valueOf(adapter.getItemCount()) + " Posts");
    }


    private static class PostHolder extends RecyclerView.ViewHolder{

        private ImageView profilePostImageView;
        public PostHolder(@NonNull View itemView) {
            super(itemView);
            profilePostImageView = itemView.findViewById(R.id.profilePostImageView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri uri = result.getUri();
            uploadImage(uri);
        }
    }

    private void uploadImage(Uri uri){
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        reference.putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            reference.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageURL = uri.toString();
                                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                            request.setPhotoUri(uri);

                                            Map<String, Object> map = new HashMap<>();
                                            map.put("profileImg", imageURL);

                                            user.updateProfile(request.build());
                                            FirebaseFirestore.getInstance().collection(("Users"))
                                                    .document(uid)
                                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(getContext(), "Update Successfully !", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void storeProfileImage(Bitmap bitmap, String url){
        SharedPreferences preferences = getActivity().getPreferences(PREF_NAME);
        boolean isStored = preferences.getBoolean(PREF_STORED, false);
        String urlString = preferences.getString(PREF_URL, "");
        SharedPreferences.Editor editor = preferences.edit();

        if (isStored && urlString.equals(url)) return;
        if (isSearching) return;

        ContextWrapper wrapper = new ContextWrapper(getContext().getApplicationContext());
        File directory = wrapper.getDir("image_data", Context.MODE_PRIVATE);

        if (!directory.exists()) directory.mkdir();

        File path = new File(directory, "profile.png");
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } finally {
            try{
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        editor.putBoolean(PREF_STORED, true);
        editor.putString(PREF_URL, url);
        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
        editor.apply();
    }

    private void loadCollectionData(View view){
        collectionList = new ArrayList<>();
        collectionAdapter = new CollectionAdapter(collectionList, getActivity(), user.getUid(), "Profile", "", "", "", null, "");
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(collectionAdapter);

        FirebaseFirestore.getInstance()
                .collection("Users").document(user.getUid())
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
    }

    private void createNewCollection(String name, String description) {
        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .collection("Collections");
        String collectionID = reference.document().getId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() / 1000, 0);

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
                    ACNameET.setText("");
                    ACDescriptionET.setText("");
//                    collectionList.add(new CollectionModel(collectionID, name, description, 0, timestamp.toDate()));
                    DocumentReference document = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
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
            Log.d("TEST !!!", "fix size: " + collectionFixSize);
            Log.d("TEST !!!", "list size: " + collectionList.size());
            collectionList.remove(0);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onStop(){
        super.onStop();
        adapter.stopListening();
    }

}
package com.example.socialmediaapp.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import static com.example.socialmediaapp.MainActivity.currentUid;
import static com.example.socialmediaapp.MainActivity.isSearching;
import static com.example.socialmediaapp.fragments.Home.fixSize;
import static com.example.socialmediaapp.utils.Constants.PREF_DIRECTORY;
import static com.example.socialmediaapp.utils.Constants.PREF_NAME;
import static com.example.socialmediaapp.utils.Constants.PREF_STORED;
import static com.example.socialmediaapp.utils.Constants.PREF_URL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.CollectionAdapter;
import com.example.socialmediaapp.adapter.HomeAdapter;
import com.example.socialmediaapp.adapter.UserAdapter;
import com.example.socialmediaapp.model.CollectionModel;
import com.example.socialmediaapp.model.HomeModel;
import com.example.socialmediaapp.model.PostModel;
import com.example.socialmediaapp.model.UserModel;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment{

    private TextView nameTV, toolbarNameTV, statusTV, followingCountTV, follwersCountTV;
    private CircleImageView profileImage;
    private Button followBtn, collectionAddBtn, shotsCountBtn, collectionsCountBtn, addfrBtn, listFriendBtn;
    private RecyclerView recyclerView, listFriendRV;
    private FirebaseUser user;
    private ImageButton editProfileBtn, settingBtn, acceptBtn, denyBtn;
    private LinearLayout countLayout, addfr_followLL;
    private Boolean isMyProfile = true;
//    FirestoreRecyclerAdapter<PostModel, PostHolder> adapter;
    private CollectionAdapter collectionAdapter;
    private List<CollectionModel> collectionList;
    public static int collectionFixSize = 0;
    private EditText ACNameET, ACDescriptionET;
    private Context context;
    private Boolean isCreating = true;
    private List<String> following, followers, myfollowing, myfollowers;
    private Boolean isFollowed;
    private DocumentReference userRef, myRef;
    private String uid;
    private ActivityResultLauncher<Intent> resultLauncher;

    private List<HomeModel> myPostList;
    private HomeAdapter myPostAdapter;
    private String imageURI, relationshipid = "";

    private OnDataPassFriend onDataPassFriend;
    private UserAdapter friendAdapter;
    private List<UserModel> friendList;
    private String currentUID;


    public Profile(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            Uri imageUri = o.getData().getData();
                            profileImage.setImageURI(imageUri);

                            uploadImage(imageUri);

                            Toast.makeText(getContext(), "Update successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "No changing profile image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        init(view);
        // Check if arguments are passed (e.g., from a notification click)
        if (getArguments() != null) {
            uid = getArguments().getString("uid");
            currentUID = getArguments().getString("currentUID");
            if (uid != null && !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                isMyProfile = false;
            } else {
                isMyProfile = true;
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        } else {
            if (isSearching) {
                uid = currentUid;
                isMyProfile = false;
            } else {
                uid = user.getUid();
                isMyProfile = true;
            }
            currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Ensure currentUID is set
        }
        if (isMyProfile){
            addfr_followLL.setVisibility(GONE);
            countLayout.setVisibility(View.VISIBLE);
            editProfileBtn.setVisibility(View.VISIBLE);
        } else {
            addfr_followLL.setVisibility(View.VISIBLE);
//            countLayout.setVisibility(View.GONE);
            editProfileBtn.setVisibility(GONE);
            settingBtn.setVisibility(GONE);
            shotsCountBtn.setVisibility(GONE);
            collectionsCountBtn.setVisibility(GONE);
        }

        userRef = FirebaseFirestore.getInstance().collection("Users").document(uid);
        myRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        loadBasicData(view);
        loadPostData(view);

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1,1)
//                        .start(getContext(), Profile.this);

                Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                resultLauncher.launch(intent);
            }
        });

        shotsCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectionAddBtn.setVisibility(GONE);
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

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFollowed){
                    followers.remove(user.getUid());
                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followers);
                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                myfollowing.remove(uid);
                                Map<String, Object> map2 = new HashMap<>();
                                map2.put("following", myfollowing);
                                myRef.update(map2);

                                followBtn.setText("Follow");
                                Toast.makeText(view.getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
                            }else {
                                Log.e("Tag", "" + task.getException().getMessage());
                            }
                        }
                    });

                } else {
                    followers.add(user.getUid());
                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followers);
                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@ NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                myfollowing.add(uid);
                                Map<String, Object> map2 = new HashMap<>();
                                map2.put("following", myfollowing);
                                myRef.update(map2);

                                followBtn.setText("Unfollow");
                                Toast.makeText(view.getContext(), "Followed", Toast.LENGTH_SHORT).show();

                            }else {
                                Log.e("Tag", "" + task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view);
            }
        });

        addfrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addfrBtn.getText().toString().equals("Add friend"))
                    addFriendAction();
                else if (addfrBtn.getText().toString().equals("Undo sending"))
                    undoSendingAddFriend();
                else if (addfrBtn.getText().toString().equals("Unfriend"))
                    unFriendAction();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptAddFriendAction();
            }
        });

        denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                denyAddFriendAction();
            }
        });

        listFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listFriendBtn.getText().toString().equals("List Friends")){
                    listFriendRV.setVisibility(View.VISIBLE);
                    listFriendBtn.setText("Show less friends");
                } else if (listFriendBtn.getText().toString().equals("Show less friends")){
                    listFriendRV.setVisibility(GONE);
                    listFriendBtn.setText("List Friends");
                }
            }
        });

        fixSize = myPostList.size();
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
        this.addfr_followLL = view.findViewById(R.id.addfr_followLL);
        this.addfrBtn = view.findViewById(R.id.addfrBtn);
        this.acceptBtn = view.findViewById(R.id.acceptBtn);
        this.denyBtn = view.findViewById(R.id.denyBtn);
        this.listFriendBtn = view.findViewById(R.id.ListFriend_Btn);
        this.listFriendRV = view.findViewById(R.id.listFriendRV);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        following = new ArrayList<>();
        followers = new ArrayList<>();
        myfollowers = new ArrayList<>();
        myfollowing = new ArrayList<>();
        fixSize = 0;
    }

    private void loadBasicData(View view) {
        // Lấy giá trị của currentUID từ FirebaseAuth nếu nó chưa được gán
        if (currentUID == null) {
            currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Load user ref error", error.getMessage());
                    return;
                }
                if (value.exists()) {
                    String name = value.getString("name");
                    String status = value.getString("status");
                    following = value.contains("following") ? (List<String>) value.get("following") : new ArrayList<>();
                    followers = value.contains("followers") ? (List<String>) value.get("followers") : new ArrayList<>();
                    String profileURL = value.getString("profileImg");
                    imageURI = profileURL;
                    int collectionCount = value.getLong("collectionCount").intValue();

                    nameTV.setText(name);
                    toolbarNameTV.setText(name);
                    statusTV.setText(status);
                    followingCountTV.setText(String.valueOf(following.size()));
                    follwersCountTV.setText(String.valueOf(followers.size()));
                    collectionsCountBtn.setText(String.valueOf(collectionCount) + " Collections");

                    int width = 200; // Đặt kích thước mong muốn
                    int height = 200; // Đặt kích thước mong muốn

                    Glide.with(context)
                            .load(profileURL)
                            .placeholder(R.drawable.profile)
                            .fitCenter() // Hoặc .centerCrop()
                            .override(width, height)
                            .timeout(6500)
                            .into(profileImage);

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse(profileURL))
                            .build();
                    user.updateProfile(profileUpdates);

                    Log.d("TEST !!!", "userfollwing size " + following.size());
                    Log.d("TEST !!!", "userfollwers size " + followers.size());
                }
            }
        });

        myRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Load my ref error", error.getMessage());
                    return;
                }
                if (value == null || !value.exists()) return;

                myfollowing = value.contains("following") ? (List<String>) value.get("following") : new ArrayList<>();
                myfollowers = value.contains("followers") ? (List<String>) value.get("followers") : new ArrayList<>();

                if (myfollowing.contains(uid)) {
                    followBtn.setText("Unfollow");
                    isFollowed = true;
                } else {
                    isFollowed = false;
                }

                Log.d("TEST !!!", "myfollwing size " + myfollowing.size());
                Log.d("TEST !!!", "myfollwers size " + myfollowers.size());
            }
        });

        FirebaseFirestore.getInstance().collection("Relationships").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Load relationships error", error.getMessage());
                    return;
                }
                if (value == null) return;

                for (QueryDocumentSnapshot snapshot : value) {
                    String uid1 = snapshot.getString("uid1");
                    String uid2 = snapshot.getString("uid2");
                    String status = snapshot.getString("status");
                    if (uid1.equals(currentUID) && uid2.equals(uid) && !status.equals("denied")) {
                        if (status.equals("waiting acceptance")) {
                            addfrBtn.setText("Undo sending");
                        } else if (status.equals("accepted") || status.equals("friend")) {
                            addfrBtn.setText("Unfriend");
                        }
                        addfrBtn.setVisibility(View.VISIBLE);
                        acceptBtn.setVisibility(GONE);
                        denyBtn.setVisibility(GONE);
                        relationshipid = snapshot.getString("id");
                        break;
                    } else if (uid1.equals(uid) && uid2.equals(currentUID) && !status.equals("denied")) {
                        if (status.equals("waiting acceptance")) {
                            addfrBtn.setVisibility(View.GONE);
                            acceptBtn.setVisibility(View.VISIBLE);
                            denyBtn.setVisibility(View.VISIBLE);
                        } else if (status.equals("accepted") || status.equals("friend")) {
                            addfrBtn.setText("Unfriend");
                            addfrBtn.setVisibility(View.VISIBLE);
                            acceptBtn.setVisibility(GONE);
                            denyBtn.setVisibility(GONE);
                        }
                        relationshipid = snapshot.getString("id");
                        break;
                    } else {
                        relationshipid = "";
                    }
                }
            }
        });

        loadListFriendData(view);
    }


    public void loadPostData(View view){

        shotsCountBtn.setBackgroundColor(Color.parseColor("#F1F1FE"));
        shotsCountBtn.setTextColor(Color.parseColor("#5151C6"));
        collectionsCountBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
        collectionsCountBtn.setTextColor(Color.parseColor("#BDBDBD"));

        myPostList = new ArrayList<>();
        myPostAdapter = new HomeAdapter(myPostList, getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(myPostAdapter);

        myPostAdapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onReacted(int position, String id, String uID, List<String> likes, List<String> hahas, List<String> sads, List<String> wows, List<String> angrys, int isChecked, int previousEmotion) {

                if (previousEmotion != isChecked) {
                    DocumentReference documentReference = FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(uid)
                            .collection("Post Images")
                            .document(id);
                    Map<String, Object> map = new HashMap<>();

                    switch (previousEmotion) {
                        case 1:
                            likes.remove(user.getUid());
                            map.put("likes", likes);
                            break;
                        case 2:
                            hahas.remove(user.getUid());
                            map.put("hahas", hahas);
                            break;
                        case 3:
                            sads.remove(user.getUid());
                            map.put("sads", sads);
                            break;
                        case 4:
                            wows.remove(user.getUid());
                            map.put("wows", wows);
                            break;
                        case 5:
                            angrys.remove(user.getUid());
                            map.put("angrys", angrys);
                            break;
                    }

                    switch (isChecked) {
                        case 1:
                            likes.add(user.getUid());
                            map.put("likes", likes);
                            break;
                        case 2:
                            hahas.add(user.getUid());
                            map.put("hahas", hahas);
                            break;
                        case 3:
                            sads.add(user.getUid());
                            map.put("sads", sads);
                            break;
                        case 4:
                            wows.add(user.getUid());
                            map.put("wows", wows);
                            break;
                        case 5:
                            angrys.add(user.getUid());
                            map.put("angrys", angrys);
                            break;
                    }
                    documentReference.update(map);
                    while (myPostList.size() > fixSize) {
                        myPostList.remove(0);
                    }
                }
            }
        });

        userRef.collection("Post Images").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("Error: ", error.getMessage());
                    return;
                }

                if (value == null) return;

                myPostList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    HomeModel model = snapshot.toObject(HomeModel.class);
                    myPostList.add(new HomeModel(
                            model.getName(),
                            model.getProfileImage(),
                            model.getImageUrl(),
                            model.getUid(),
                            model.getDescription(),
                            model.getId(),
                            model.getLikes(),
                            model.getHahas(),
                            model.getSads(),
                            model.getWows(),
                            model.getAngrys(),
                            model.getTimeStamp(),
                            model.getCommentCount()
                    ));
                    Log.d("TEST !!!", "my post size " + myPostList.size());
                    shotsCountBtn.setText(String.valueOf(myPostList.size()) + " Posts");
                }
                Collections.sort(myPostList, new Comparator<HomeModel>() {
                    @Override
                    public int compare(HomeModel o1, HomeModel o2) {
                        return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                    }
                });
                myPostAdapter.notifyDataSetChanged();
            }
        });
        while (myPostList.size() > fixSize){
            myPostList.remove(0);
        }

//        Query query = userRef.collection("Post Images");
//        FirestoreRecyclerOptions<PostModel> options = new FirestoreRecyclerOptions.Builder<PostModel>()
//                .setQuery(query, PostModel.class)
//                .build();
//        adapter = new FirestoreRecyclerAdapter<PostModel, PostHolder>(options) {
//            @NonNull
//            @Override
//            public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_post_item, parent, false);
//                return new PostHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull PostHolder postHolder, int i, @NonNull PostModel postModel) {
//                Glide.with(postHolder.itemView.getContext().getApplicationContext())
//                        .load(postModel.getImageUrl())
//                        .timeout(6500)
//                        .into(postHolder.profilePostImageView);
//
//            }
//
//        };
//        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
//        recyclerView.setAdapter(adapter);
//        shotsCountBtn.setText(String.valueOf(adapter.getItemCount()) + " Posts");
    }


//    private static class PostHolder extends RecyclerView.ViewHolder{
//
//        private ImageView profilePostImageView;
//        public PostHolder(@NonNull View itemView) {
//            super(itemView);
//            profilePostImageView = itemView.findViewById(R.id.profilePostImageView);
//        }
//    }

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
//                && resultCode == RESULT_OK){
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            Uri uri = result.getUri();
//            uploadImage(uri);
//        }
//    }
//
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
                                            myRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(getContext(), "Update Successfully !", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            Map<String, Object> map2 = new HashMap<>();
                                            map2.put("profileImage", imageURL);
                                            myRef.collection("Post Images").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    for(QueryDocumentSnapshot snapshot : value){
                                                        snapshot.getReference().update(map2);
                                                    }
                                                }
                                            });

                                            Map<String, Object> map3 = new HashMap<>();
                                            map3.put("profileImage", imageURL);

                                            if (recyclerView.getAdapter() instanceof HomeAdapter){
                                                loadPostData(getView());
                                            }

                                            Map<String, Object> map41 = new HashMap<>();
                                            map41.put("avt1", imageURL);
                                            Map<String, Object> map42 = new HashMap<>();
                                            map42.put("avt2", imageURL);

                                            FirebaseFirestore.getInstance().collection("Relationships").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if (error != null){
                                                        Log.e("Error: ", error.getMessage());
                                                        return;
                                                    }
                                                    if (value == null) return;
                                                    for (QueryDocumentSnapshot snapshot : value){
                                                        if (snapshot.getString("uid1").equals(user.getUid()))
                                                            FirebaseFirestore.getInstance().collection("Relationships").document(snapshot.getString("id")).update(map41);
                                                        else if (snapshot.getString("uid2").equals(user.getUid()))
                                                            FirebaseFirestore.getInstance().collection("Relationships").document(snapshot.getString("id")).update(map42);
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
//
//    private void storeProfileImage(Bitmap bitmap, String url){
//        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        boolean isStored = preferences.getBoolean(PREF_STORED, false);
//        String urlString = preferences.getString(PREF_URL, "");
//        SharedPreferences.Editor editor = preferences.edit();
//
//        if (isStored && urlString.equals(url)) return;
//        if (isSearching) return;
//
//        ContextWrapper wrapper = new ContextWrapper(getContext().getApplicationContext());
//        File directory = wrapper.getDir("image_data", Context.MODE_PRIVATE);
//
//        if (!directory.exists()) directory.mkdir();
//
//        File path = new File(directory, "profile.png");
//        FileOutputStream outputStream = null;
//
//        try {
//            outputStream = new FileOutputStream(path);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//        } catch (FileNotFoundException e){
//            e.printStackTrace();
//        } finally {
//            try{
//                assert outputStream != null;
//                outputStream.close();
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//        }
//
//        editor.putBoolean(PREF_STORED, true);
//        editor.putString(PREF_URL, url);
//        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
//        editor.apply();
//    }

    private void loadCollectionData(View view){

        collectionsCountBtn.setBackgroundColor(Color.parseColor("#F1F1FE"));
        collectionsCountBtn.setTextColor(Color.parseColor("#5151C6"));
        shotsCountBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
        shotsCountBtn.setTextColor(Color.parseColor("#BDBDBD"));

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

    private void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.dialog_setting, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        Button logoutButton = popupView.findViewById(R.id.logoutBtn);
        Switch keepSilentSwitch = popupView.findViewById(R.id.silentSwitch);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Logging out", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();

                FirebaseAuth.getInstance().signOut();
                context.startActivity(new Intent(getContext(), FragmentReplacerActivity.class));
            }
        });

        keepSilentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Cái này để tắt tiếng thông báo hay tin nhắn
                    Toast.makeText(getContext(), "Keep Silent On", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Keep Silent Off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        popupWindow.showAsDropDown(view);
    }
    private void addToHisNotifications(String hisUid, String message, String senderUid) {
        String timestamp = "" + System.currentTimeMillis();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(senderUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String sName = document.getString("name");
                    String sImage = document.getString("profileImg");

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("pId", "");
                    hashMap.put("timestamp", timestamp);
                    hashMap.put("pUId", hisUid);
                    hashMap.put("notification", message);
                    hashMap.put("sUid", senderUid);
                    hashMap.put("sName", sName);
                    hashMap.put("sImage", sImage);

                    db.collection("Users").document(hisUid).collection("Notifications").document(timestamp)
                            .set(hashMap)
                            .addOnSuccessListener(unused -> {
                                // Thông báo được thêm thành công
                            })
                            .addOnFailureListener(e -> {
                                // Thêm thông báo thất bại
                            });
                }
            }
        });
    }



    private void addFriendAction(){
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Relationships");
        String relationshipID = reference.document().getId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() / 1000, 0);
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()){
                    String name1 = value.getString("name");
                    String avt1 = value.getString("profileImg");

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", relationshipID);
                    map.put("uid1", user.getUid());
                    map.put("name1", name1);
                    map.put("avt1", avt1);
                    map.put("uid2", uid);
                    map.put("name2", toolbarNameTV.getText().toString());
                    map.put("avt2", imageURI);
                    map.put("status", "waiting acceptance"); // đã kết bạn thì là is friend
                    map.put("timestamp", timestamp);

                    reference.document(relationshipID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getContext(), "An invitation has been sent", Toast.LENGTH_SHORT).show();
                                addToHisNotifications(uid, "You have a new friend request from " + name1, user.getUid());
                                Log.d("Profile", "Friend request sent to " + uid);

                            } else {
                                Toast.makeText(getContext(), "Failed to send an add friend invitation", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    addfrBtn.setText("Undo sending");
                    relationshipid = relationshipID;
                }
            }
        });


    }

    private void undoSendingAddFriend(){
        FirebaseFirestore.getInstance().collection("Relationships").document(relationshipid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "The invitation was deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to undo sending", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addfrBtn.setText("Add friend");
        relationshipid = "";
    }

    private void denyAddFriendAction(){
        Map<String, Object> map = new HashMap<>();
        map.put("status", "denied");
        map.put("timestamp", new Timestamp(System.currentTimeMillis() / 1000, 0));

        FirebaseFirestore.getInstance().collection("Relationships").document(relationshipid).update(map);
        addfrBtn.setVisibility(View.VISIBLE);
        acceptBtn.setVisibility(GONE);
        denyBtn.setVisibility(GONE);
        addfrBtn.setText("Add friend");
        relationshipid = "";
    }

    private void acceptAddFriendAction(){
        Map<String, Object> map = new HashMap<>();
        map.put("status", "accepted");
        map.put("timestamp", new Timestamp(System.currentTimeMillis() / 1000, 0));

        FirebaseFirestore.getInstance().collection("Relationships").document(relationshipid).update(map);
        addfrBtn.setVisibility(View.VISIBLE);
        acceptBtn.setVisibility(GONE);
        denyBtn.setVisibility(GONE);
        addfrBtn.setText("Unfriend");
        addToHisNotifications(uid, "Your friend request has been accepted by " + user.getDisplayName(), user.getUid());

        // Cập nhật lại giao diện
        loadBasicData(getView());
    }

    private void unFriendAction(){
        FirebaseFirestore.getInstance().collection("Relationships").document(relationshipid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "You have just unfriended this account", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to unfriend", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addfrBtn.setText("Add friend");
        relationshipid = "";
    }

    public interface OnDataPassFriend{
        void onChange(String uID);
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        onDataPassFriend = (OnDataPassFriend) context;
    }

    private void loadListFriendData(View view){

        friendList = new ArrayList<>();
        listFriendRV.setHasFixedSize(true);
        listFriendRV.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
        int layoutID = R.layout.item_search_friend;
        friendAdapter = new UserAdapter(friendList, layoutID);
        listFriendRV.setAdapter(friendAdapter);

        FirebaseFirestore.getInstance().collection("Relationships").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.d("Error", error.getMessage().toString());
                    return;
                }
                if (value == null) return;

                friendList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String status = snapshot.getString("status");
                    String uid1 = snapshot.getString("uid1");
                    String uid2 = snapshot.getString("uid2");
                    UserModel model = new UserModel();
                    if (status.equals("friend") || status.equals("accepted")){
                        if (uid1.equals(uid)){
                            model.setuID(uid2);
                            model.setName(snapshot.getString("name2"));
                            model.setProfileImg(snapshot.getString("avt2"));
                            model.setEmail("");
                            model.setStatus("");
                            friendList.add(model);
                        } else if (uid2.equals(uid)){
                            model.setuID(uid1);
                            model.setName(snapshot.getString("name1"));
                            model.setProfileImg(snapshot.getString("avt1"));
                            model.setEmail("");
                            model.setStatus("");
                            friendList.add(model);
                        }
                    }
                }
                friendAdapter.notifyDataSetChanged();
            }
        });

        friendAdapter.OnProfileChosen(new UserAdapter.OnProfileChosen() {
            @Override
            public void onChosen(String uID) {
                onDataPassFriend.onChange(uID);
            }
        });
    }

    private void chooseProfile(){
        friendAdapter.OnProfileChosen(new UserAdapter.OnProfileChosen() {
            @Override
            public void onChosen(String uID) {
                onDataPassFriend.onChange(uID);
            }
        });
    }

//    @Override
//    public void onStart(){
//        super.onStart();
//        adapter.startListening();
//    }
//    @Override
//    public void onStop(){
//        super.onStop();
//        adapter.stopListening();
//    }

}
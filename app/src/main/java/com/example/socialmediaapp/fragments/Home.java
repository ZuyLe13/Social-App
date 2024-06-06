package com.example.socialmediaapp.fragments;

import static com.example.socialmediaapp.MainActivity.MyName;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.FragmentTransitionSupport;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.MessengerActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.SplashActivity;
import com.example.socialmediaapp.adapter.HomeAdapter;
import com.example.socialmediaapp.model.HomeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Home extends Fragment {

    private RecyclerView postRecycleView;
    private List<HomeModel> popularPostList, trendingPostList, followingPostList;
    private HomeAdapter popularAdapter, trendingAdapter, followingAdapter;
    private FirebaseUser user;
    private Parcelable recyclerViewState;
    static public int fixSize = 0;
    private Button popularBtn, trendingBtn, followingBtn;
    private ListenerRegistration popularListener, trendingListener, followingListener;
    private Boolean hadLoaded = false;
    private int popularFixSize = 0, trendingFixSize = 0, followingFixSize = 0;

    private ImageButton messageBtn;


    public Home(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState){
        super.onViewCreated(view, saveInstanceState);

        init(view);

        popularBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPopularData();
                setFocusBtnColor(popularBtn);
                setNormalBtnColor(trendingBtn);
                setNormalBtnColor(followingBtn);
            }
        });
        trendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTrendingData();
                setFocusBtnColor(trendingBtn);
                setNormalBtnColor(popularBtn);
                setNormalBtnColor(followingBtn);
            }
        });
        followingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFollowingData();
                setFocusBtnColor(followingBtn);
                setNormalBtnColor(trendingBtn);
                setNormalBtnColor(popularBtn);
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang MessengerActivity từ Fragment
//                startActivity(new Intent(getActivity(), MessengerActivity.class));

            }
        });

        loadFollowingData();
        loadTrendingData();
        loadPopularData();
        hadLoaded = true;
    }

    private void init(View view){
        postRecycleView = view.findViewById(R.id.postRecycleView);
        postRecycleView.setHasFixedSize(true);
        postRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        popularBtn = view.findViewById(R.id.popularBtn);
        trendingBtn = view.findViewById(R.id.trendBtn);
        followingBtn = view.findViewById(R.id.followBtn);

        messageBtn = view.findViewById(R.id.messageBtn);

//        authTemp("21522605@gm.uit.edu.vn", "123456");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        popularPostList = new ArrayList<>();
        trendingPostList = new ArrayList<>();
        followingPostList = new ArrayList<>();

        popularAdapter = new HomeAdapter(popularPostList, getActivity());
        trendingAdapter = new HomeAdapter(trendingPostList, getActivity());
        followingAdapter = new HomeAdapter(followingPostList, getActivity());
    }

    public void authTemp(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        user = auth.getCurrentUser();
                    } else {
                        Log.e("Auth Error", "Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void resetAdapter(List<HomeModel> postList, HomeAdapter adapter, String type){
        fixSize = 0;
        postList.clear();
        postRecycleView.setAdapter(adapter);
        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onReacted(int position, String id, String uID, List<String> reacts, int isChecked) {

                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uID)
                        .collection("Post Images")
                        .document(id);

                if (reacts.contains(user.getUid())){
                    reacts.remove(user.getUid());
                } else {
                    reacts.add(user.getUid());
                }

                Map<String, Object> map = new HashMap<>();
                map.put("reacts", reacts);
                documentReference.update(map);

//                if (type.equals("popular")) loadPopularData();
//                else if (type.equals("trending")) loadTrendingData();
//                else if (type.equals("following")) loadFollowingData();
            }

        });
    }

    private void loadFollowingData(){
        fixSize = followingFixSize;
        unregisterListeners();
        resetAdapter(followingPostList, followingAdapter, "following");
        CollectionReference coRef = FirebaseFirestore.getInstance().collection("Users");

        followingListener = coRef.document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.d("TEST !!!", "Error: " + error.getMessage());
                }

                if (value == null){
                    return;
                }

                MyName = value.getString("name");
                List<String> following = (List<String>)value.get("following");

                if (following == null || following.isEmpty()) return;

                followingPostList.clear();

                coRef.whereIn("uID", following)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {

                                if (error != null){
                                    Log.e("Error: ", error.getMessage());
                                    return;
                                }
                                if (value2 == null) return;

                                for (QueryDocumentSnapshot snapshot : value2){
                                    if (!snapshot.exists()) return;

                                    snapshot.getReference().collection("Post Images")
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value3, @Nullable FirebaseFirestoreException error) {
                                                    if (error != null){
                                                        Log.e("Error: ", error.getMessage());
                                                        return;
                                                    }

                                                    if (value3 == null) return;

                                                    for (QueryDocumentSnapshot snapshot2 : value3) {
                                                        HomeModel model = snapshot2.toObject(HomeModel.class);
                                                        followingPostList.add(new HomeModel(
                                                                model.getName(),
                                                                model.getProfileImage(),
                                                                model.getImageUrl(),
                                                                model.getUid(),
                                                                model.getDescription(),
                                                                model.getId(),
                                                                model.getReacts(),
                                                                model.getTimeStamp(),
                                                                model.getCommentCount()
                                                        ));
                                                    }

                                                    Collections.sort(followingPostList, new Comparator<HomeModel>() {
                                                        @Override
                                                        public int compare(HomeModel o1, HomeModel o2) {
                                                            return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                                                        }
                                                    });
                                                    if (!hadLoaded)
                                                        followingFixSize = followingPostList.size();
                                                    else if (followingPostList.size() != followingFixSize)
                                                        removeDuplicates(followingPostList);
                                                    Log.d("TEST !!!", "following size: " + fixSize);
                                                    followingAdapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });
                while (followingPostList.size() > fixSize){
                    followingPostList.remove(0);
                }
            }
        });
//        followingPostList = removeDuplicates(followingPostList);
    }

    private void loadPopularData(){
        fixSize = popularFixSize;
        unregisterListeners();
        resetAdapter(popularPostList, popularAdapter, "popular");
        popularListener =  FirebaseFirestore.getInstance().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) { Log.d("TEST !!!", "Error: " + error.getMessage()); return; }
                if (value == null) return;

                popularPostList.clear();

                for (QueryDocumentSnapshot snapshot : value) {
                    String userID = snapshot.getString("uID");
                    FirebaseFirestore.getInstance().collection("Users").document(userID).collection("Post Images").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value3, @Nullable FirebaseFirestoreException error) {
                            if (error != null){
                                Log.e("Error: ", error.getMessage());
                                return;
                            }

                            if (value3 == null) return;

                            for (QueryDocumentSnapshot snapshot2 : value3) {
                                HomeModel model = snapshot2.toObject(HomeModel.class);
                                popularPostList.add(new HomeModel(
                                        model.getName(),
                                        model.getProfileImage(),
                                        model.getImageUrl(),
                                        model.getUid(),
                                        model.getDescription(),
                                        model.getId(),
                                        model.getReacts(),
                                        model.getTimeStamp(),
                                        model.getCommentCount()
                                ));
                            }
                            Collections.sort(popularPostList, new Comparator<HomeModel>() {
                                @Override
                                public int compare(HomeModel o1, HomeModel o2) {
                                    return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                                }
                            });
                            if (!hadLoaded)
                                popularFixSize = popularPostList.size();
                            else if (popularPostList.size() != popularFixSize)
                                removeDuplicates(popularPostList);
                            popularAdapter.notifyDataSetChanged();
                            Log.d("TEST !!!", "popular size: " + popularPostList.size());
                        }
                    });
                }
                while (popularPostList.size() > 6){
                    popularPostList.remove(0);
                }
            }
        });
//        popularPostList = removeDuplicates(popularPostList);
    }

    private void loadTrendingData(){
        fixSize = trendingFixSize;
        unregisterListeners();
        resetAdapter(trendingPostList, trendingAdapter, "trending");
        trendingListener = FirebaseFirestore.getInstance().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) { Log.d("TEST !!!", "Error: " + error.getMessage()); return; }
                if (value == null) return;

                trendingPostList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String userID = snapshot.getString("uID");
                    FirebaseFirestore.getInstance().collection("Users").document(userID).collection("Post Images").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value3, @Nullable FirebaseFirestoreException error) {
                            if (error != null){
                                Log.e("Error: ", error.getMessage());
                                return;
                            }

                            if (value3 == null) return;

                            for (QueryDocumentSnapshot snapshot2 : value3) {
                                HomeModel model = snapshot2.toObject(HomeModel.class);
                                trendingPostList.add(new HomeModel(
                                        model.getName(),
                                        model.getProfileImage(),
                                        model.getImageUrl(),
                                        model.getUid(),
                                        model.getDescription(),
                                        model.getId(),
                                        model.getReacts(),
                                        model.getTimeStamp(),
                                        model.getCommentCount()
                                ));
                            }
                            Collections.sort(trendingPostList, new Comparator<HomeModel>() {
                                @Override
                                public int compare(HomeModel o1, HomeModel o2) {
                                    return Integer.compare(o2.getReacts().size(), o1.getReacts().size());
                                }
                            });
                            if (!hadLoaded)
                                trendingFixSize = trendingPostList.size();
                            else if (trendingPostList.size() != trendingFixSize)
                                removeDuplicates(trendingPostList);
                            trendingAdapter.notifyDataSetChanged();
                            Log.d("TEST !!!", "trending size: " + fixSize);
                        }
                    });
                }
                while (trendingPostList.size() > fixSize){
                    trendingPostList.remove(0);
                }
            }
        });
//        trendingPostList = removeDuplicates(trendingPostList);
    }

    public void setFocusBtnColor(Button button){
        button.setBackgroundColor(Color.parseColor("#F1F1FE"));
        button.setTextColor(Color.parseColor("#5151C6"));
    }
    public void setNormalBtnColor(Button button){
        button.setBackgroundColor(Color.parseColor("#FFFFFF"));
        button.setTextColor(Color.parseColor("#BDBDBD"));
    }

    private void unregisterListeners() {
        if (popularListener != null) {
            popularListener.remove();
            popularListener = null;
        }
        if (trendingListener != null) {
            trendingListener.remove();
            trendingListener = null;
        }
        if (followingListener != null) {
            followingListener.remove();
            followingListener = null;
        }
    }

    private void removeDuplicates(List<HomeModel> list) {
        HashSet<String> seenIds = new HashSet<>();
        int i = 0;
        while (i < list.size()) {
            HomeModel item = list.get(i);
            if (seenIds.contains(item.getId())) {
                list.remove(i);
            } else {
                seenIds.add(item.getId());
                i++;
            }
        }
    }



}
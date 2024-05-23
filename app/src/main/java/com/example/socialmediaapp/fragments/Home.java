package com.example.socialmediaapp.fragments;

import static com.example.socialmediaapp.MainActivity.MyName;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Home extends Fragment {

    private RecyclerView postRecycleView;
    private List<HomeModel> postList;
    private HomeAdapter adapter;
    private FirebaseUser user;
    private Parcelable recyclerViewState;
    private int fixSize = 0;

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

        postList = new ArrayList<>();
        adapter = new HomeAdapter(postList, getActivity());
        postRecycleView.setAdapter(adapter);

        loadDataFromFireStore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onReacted(int position, String id, String uID, List<String> reacts, int isChecked) {

                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(user.getUid())
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
                while (postList.size() != fixSize){
                    postList.remove(0);
                }
            }

//            @Override
//            public void onCommented(int position, String id, String uID, String comment, LinearLayout commentLL, EditText commentET) {
//
//
////                while (postList.size() != fixSize){
////                    postList.remove(0);
////                }
//            }
        });
    }

    private void init(View view){
        postRecycleView = view.findViewById(R.id.postRecycleView);
        postRecycleView.setHasFixedSize(true);
        postRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));


//        authTemp("21522605@gm.uit.edu.vn", "123456");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
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


    private void loadDataFromFireStore(){

        if (user == null) {
            Log.e("Firestore Error", "User is not authenticated.");
            return;
        }

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());
        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

        final int[] i = {0};
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                postList.clear();

                if (following == null || following.isEmpty()) return;


                collectionReference.whereIn("uID", following)
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
                                                        postList.add(new HomeModel(
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
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });
            }
        });

     fixSize = postList.size();
    }

}
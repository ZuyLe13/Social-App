package com.example.socialmediaapp.fragments;

import static com.example.socialmediaapp.MainActivity.MyName;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.HomeAdapter;
import com.example.socialmediaapp.model.HomeModel;
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
import java.util.List;

public class Home extends Fragment {

    private RecyclerView postRecycleView;
    private List<HomeModel> postList;
    private HomeAdapter adapter;
    private FirebaseUser user;
    public static int LIST_SIZE = 0;

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
        adapter = new HomeAdapter(postList, getContext());
        postRecycleView.setAdapter(adapter);

        loadDataFromFireStore();
    }

    private void init(View view){
//        Toolbar toolbar = view.findViewById(R.id.homeToolbar);
//        if (getActivity() != null) {
//            ((AppCompatActivity) getActivity()).setSupportActionBar(homeToolbar);
//        }
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
                        // Đăng nhập thành công, cập nhật người dùng hiện tại
                        user = auth.getCurrentUser();
                        // Sau khi đăng nhập thành công, gọi phương thức loadDataFromFireStore
                        loadDataFromFireStore();
                    } else {
                        // Nếu đăng nhập thất bại, in ra thông báo lỗi
                        Log.e("Auth Error", "Authentication failed: " + task.getException().getMessage());
                    }
                });
    }


    private void loadDataFromFireStore(){
//        postList.add(new HomeModel("Hoang Tran", "01/04/2024", "", "", "123456", 1, 10));
//        postList.add(new HomeModel("Hoang Tran", "02/04/2024", "", "", "223456", 2, 20));
//        postList.add(new HomeModel("Hoang Tran", "03/04/2024", "", "", "323456", 3, 30));
//        postList.add(new HomeModel("Hoang Tran", "04/04/2024", "", "", "423456", 4, 40));

        if (user == null) {
            Log.e("Firestore Error", "User is not authenticated.");
            return;
        }

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())
                .collection("Post Images");
        reference.addSnapshotListener(new EventListener<QuerySnapshot>(){
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("Error: ", error.getMessage());
                    return;
                }

                if (value == null){
                    return;
                }

                postList.clear();

                for (QueryDocumentSnapshot snapshot : value){

                    if (!snapshot.exists()) {
                        return;
                    }
                    HomeModel model = snapshot.toObject(HomeModel.class);
                    postList.add(new HomeModel(
                            model.getName(),
                            model.getProfileImage(),
                            model.getImageUrl(),
                            model.getUid(),
                            model.getComments(),
                            model.getDescription(),
                            model.getId(),
                            model.getReactList(),
                            model.getTimeStamp()
                    ));
                }

                adapter.notifyDataSetChanged();
            }
        });

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    return;
                }
                if (value.exists()){
                    MyName = value.getString("name");
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadDataFromFireStore();
    }
}
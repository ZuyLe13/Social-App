package com.example.socialmediaapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.UserAdapter;
import com.example.socialmediaapp.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Search extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userModelList;
    private CollectionReference reference;
    private OnDataPass onDataPass;

    public interface OnDataPass{
        void onChange(String uID);
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        onDataPass = (OnDataPass) context;
    }

    public Search(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);

    }

    @Override
    public void onViewCreated(@Nonnull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        init(view);
        loadUsersData();
        searchUsers();
        chooseProfile();
    }

    private void init(View view){
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userModelList = new ArrayList<>();
        userAdapter = new UserAdapter(userModelList);
        recyclerView.setAdapter(userAdapter);
    }

    private void loadUsersData(){
        reference = FirebaseFirestore.getInstance().collection("Users");
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.d("TEST !!!", "We got an error onEvent snapshot");
                    return;
                }

                if (value == null) {
                    Log.d("TEST !!!", "We got value = null onEvent snapshot");
                    return;
                }

                for (QueryDocumentSnapshot snapshot : value){
                    UserModel model = snapshot.toObject(UserModel.class);
                    if (!model.getuID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        userModelList.add(model);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }


    private void searchUsers(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                reference.orderBy("name").startAt(s).endAt(s + "\uf8ff")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    userModelList.clear();
                                    for (DocumentSnapshot snapshot : task.getResult()){
                                        if (!snapshot.exists()){
                                            Log.d("TEST !!!", "Snapshot does not exist");
                                            return;
                                        }

                                        UserModel model = snapshot.toObject(UserModel.class);
                                        if (!model.getuID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            userModelList.add(model);
                                        }
                                    }
                                    userAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")){
                    userModelList.clear();
                    loadUsersData();
                }
                return false;
            }
        });
    }

    private void chooseProfile(){
        userAdapter.OnProfileChosen(new UserAdapter.OnProfileChosen() {
            @Override
            public void onChosen(String uID) {
                onDataPass.onChange(uID);
            }
        });
    }
}
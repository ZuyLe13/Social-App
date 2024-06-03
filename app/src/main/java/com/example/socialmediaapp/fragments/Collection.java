package com.example.socialmediaapp.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.CollectionPostAdapter;
import com.example.socialmediaapp.model.CollectionModel;
import com.example.socialmediaapp.model.CollectionPostModel;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Collection extends Fragment {

    private List<CollectionPostModel> postList;
    private CollectionPostAdapter adapter;
    private String collectionID, collectionName, currentUID;
    public static int CPFixSize = 0;

    private TextView titleTV;
    private ImageButton backBtn;
    private RecyclerView collectionRV;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        init(view);
        loadDataFromFirestore();
    }

    private void init(View view){
        titleTV = view.findViewById(R.id.collectionTitleTV);
        backBtn = view.findViewById(R.id.collectionBackBtn);
        collectionRV = view.findViewById(R.id.collectionRV);

        currentUID = getArguments().getString("collectionUID");
        collectionID = getArguments().getString("collectionID");
        collectionName = getArguments().getString("collectionName");

        titleTV.setText(collectionName);

        postList = new ArrayList<>();
        collectionRV.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new CollectionPostAdapter(postList, getActivity(), currentUID, collectionID);
        collectionRV.setAdapter(adapter);
    }

    private void loadDataFromFirestore(){
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUID)
                .collection("Collections")
                .document(collectionID)
                .collection("CollectionItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.e("Error: ", error.getMessage());
                            return;
                        }

                        if (value == null) return;

                        postList.clear();
                        for (QueryDocumentSnapshot snapshot : value) {
                            CollectionPostModel model = snapshot.toObject(CollectionPostModel.class);
                            postList.add(model);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        CPFixSize = postList.size();
    }
}

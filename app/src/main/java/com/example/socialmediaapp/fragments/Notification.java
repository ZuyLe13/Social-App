package com.example.socialmediaapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.NotificationAdapter;
import com.example.socialmediaapp.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Notification extends Fragment {
    RecyclerView notificationsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<NotificationModel> notiList;
    private NotificationAdapter notificationAdapter;

    public Notification(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationsRv = view.findViewById(R.id.recyclerView);
        firebaseAuth = FirebaseAuth.getInstance();

        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        notiList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(firebaseAuth.getUid()).collection("Notifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                NotificationModel model = document.toObject(NotificationModel.class);
                                notiList.add(model);
                            }
                            notificationAdapter = new NotificationAdapter(getActivity(), notiList);
                            notificationsRv.setAdapter(notificationAdapter);
                        }
                    }
                });
    }

}
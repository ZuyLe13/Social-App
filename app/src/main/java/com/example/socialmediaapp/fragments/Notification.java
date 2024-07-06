package com.example.socialmediaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.NotificationAdapter;
import com.example.socialmediaapp.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Notification extends Fragment {
    RecyclerView notificationsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<NotificationModel> notiList;
    private NotificationAdapter notificationAdapter;
    private TextView countNotification;
//    private OnDataPass onDataPass;
//
//    public interface OnDataPass{
//        void onChange(String uID);
//    }

//    @Override
//    public void onAttach(@NonNull Context context){
//        super.onAttach(context);
//        onDataPass = (OnDataPass) context;
//    }

    public Notification(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationsRv = view.findViewById(R.id.recyclerView);
        countNotification = view.findViewById(R.id.countNotification); // Ánh xạ TextView
        firebaseAuth = FirebaseAuth.getInstance();

        notificationsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        notiList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notiList);
        notificationsRv.setAdapter(notificationAdapter);

        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(firebaseAuth.getUid()).collection("Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)  // Sắp xếp theo thời gian giảm dần
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        notiList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            NotificationModel model = document.toObject(NotificationModel.class);
                            notiList.add(model);
                        }
                        notificationAdapter.notifyDataSetChanged();
                        countNotification.setText("(" + notiList.size() + ")");  // Cập nhật số lượng thông báo
                    }
                });
    }


//    private void chooseProfile(){
//        notificationAdapter.OnProfileChosen(new notificationAdapter.OnProfileChosen() {
//            @Override
//            public void onChosen(String uID) {
//                onDataPass.onChange(uID);
//            }
//        });
//    }
}
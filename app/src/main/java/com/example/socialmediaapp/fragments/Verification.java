package com.example.socialmediaapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.socialmediaapp.R;

public class Verification extends Fragment {
    private EditText verifyCodeEt;
    private TextView dontReceiveTv;
    private Button verifyBtn;
    public Verification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }
    private void init(View v){
//        Log.d("init123466", "uploadUser: User data uploaded successfully");


        verifyCodeEt = v.findViewById(R.id.verifyET);
        dontReceiveTv = v.findViewById(R.id.receiveCodeTV);

        verifyBtn = v.findViewById(R.id.verifyBtn);

//        progressBar = v.findViewById(R.id.progressBar);
//        auth = FirebaseAuth.getInstance();
    }
    private void clickListener() {
        verifyBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                loadFragment(new SignUp(), true);
//                ((FragmentReplacerActivity) getActivity()).setFragment(new SignUp());
            }
        });
    }
}

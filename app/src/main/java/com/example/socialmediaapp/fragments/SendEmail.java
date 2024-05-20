package com.example.socialmediaapp.fragments;

import static com.example.socialmediaapp.fragments.SignIn.pattern;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SendEmail extends Fragment {

    private EditText emailEt;
    private Button sendBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    public SendEmail() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }
    private void init(View v){
//        Log.d("init123466", "uploadUser: User data uploaded successfully");
        emailEt = v.findViewById(R.id.emailET);


        sendBtn = v.findViewById(R.id.verifyBtn);
        progressBar = v.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }
    private void clickListener() {
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();

                if (email.isEmpty() || !pattern.matcher(email).matches()){
                    emailEt.setError("Email is invalid");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(),"Password reset email has been sent",
                                            Toast.LENGTH_SHORT).show();
                                    emailEt.setText("");
                                    progressBar.setVisibility(View.GONE);
                                    gotoSignInPage();
                                }
                                else{
                                    String ext = "Error: " + task.getException().getMessage();
                                    Toast.makeText(getContext(),ext, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void gotoSignInPage() {
        if (getContext() != null && getActivity() != null) {
            Activity activity = getActivity();
//            progressBar.setVisibility(View.GONE);
            if (activity instanceof FragmentReplacerActivity) {

                ((FragmentReplacerActivity) getActivity()).setFragment(new SignIn());
            }
            else {
                Log.d("not instanceof FragmentReplacerActivity", "" + getActivity());

            }

        }
        else{
            Toast.makeText(getContext(), "Go to Verification Page failed", Toast.LENGTH_SHORT).show();

        }
    }
}
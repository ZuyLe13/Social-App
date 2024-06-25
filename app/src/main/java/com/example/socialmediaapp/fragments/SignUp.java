package com.example.socialmediaapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUp extends Fragment {

    private EditText nameEt, phoneEt, emailEt, passwEt, cfpasswEt;
    private Button signUpbtn;
    private ProgressBar progressBar;
    private TextView signInTv;
    private FirebaseAuth auth;

    public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
    public SignUp() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("onCreateView", "onCreateView1");
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("onViewCreated", "onViewCreated1");
        init(view);

        clickListener();
    }
    private void init(View v){
        Log.d("init123466", "uploadUser: User data uploaded successfully");

        nameEt= v.findViewById(R.id.fullNameET);
        phoneEt = v.findViewById(R.id.phoneNumberET);
        emailEt = v.findViewById(R.id.emailET);
        passwEt = v.findViewById(R.id.passwordET);
        cfpasswEt = v.findViewById(R.id.confirmPassET);
        signUpbtn = v.findViewById(R.id.signupBtn);
        signInTv = v.findViewById(R.id.signinBtn);

        progressBar = v.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }
    private void clickListener(){
        signInTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoSignInPage();
            }
        });
        signUpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Processing", Toast.LENGTH_SHORT).show();

                String name = nameEt.getText().toString();
                String phone = phoneEt.getText().toString();
                String mail = emailEt.getText().toString();
                String pw = passwEt.getText().toString();
                String cf_pw = cfpasswEt.getText().toString();

                if (name.isEmpty() || name.equals(" ")){
                    nameEt.setError("Name can not be empty");
                    return;
                }
                if (phone.isEmpty() || phone.equals(" ")){
                    phoneEt.setError("Phone number can not be empty");
                    return;
                }
                else if (!android.util.Patterns.PHONE.matcher(phone).matches() || phone.length() != 10) {
                    phoneEt.setError("Invalid phone number");
                    return;
                }

                if (mail.isEmpty() || !pattern.matcher(mail).matches()){
                    emailEt.setError("Email is invalid");
                    return;
                }
                if (pw.isEmpty() || pw.length() <6){
                    passwEt.setError("Password is invalid");
                    return;
                }
                if (cf_pw.isEmpty() || cf_pw.length() <6 ){
                    cfpasswEt.setError("Confirmed password is invalid");
                    return;
                }
                if (!cf_pw.equals(pw)){
                    cfpasswEt.setError("Passwords do not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                createAccount (name,phone, mail, pw);
                gotoSignInPage();
            }
        });
    }
    private void createAccount (String name,String phone, String email, String pw){
        auth.createUserWithEmailAndPassword(email,pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(getContext(),"Email verification link has been sent", Toast.LENGTH_SHORT).show();

                                                uploadUser(user,phone,name,email);
                                                Toast.makeText(getContext(),"Sign up successful", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(getContext(),"Can not send email verification link", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });



                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            String ext = task.getException().getMessage();
                            Log.d("createAccount", "Error:"+ext);
                            Toast.makeText(getContext(),"Sign up unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void uploadUser(FirebaseUser user,String phone,  String name,String email){
        Map<String, Object> map = new HashMap<>();
        map.put("name",name);
        map.put("phone_num",phone);
        map.put("email",email);
        map.put("profileImg","");
        map.put("uID",user.getUid());
        map.put("followers",new ArrayList<>());
        map.put("following",new ArrayList<>());
        map.put("status","offline");
//        map.put("active","offline");
        map.put("collectionCount", 0);

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
//                            startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
//                            getActivity().finish();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse("https://toppng.com/uploads/preview/person-vector-11551054765wbvzeoxz2c.png"))
                .build();
        user.updateProfile(profileUpdates);
    }
    private void gotoSignInPage (){
        if (getContext() != null && getActivity() != null) {
            Log.d("SigninFragment", "uploadUser: User data uploaded successfully");
//                    Toast.makeText(getContext(), "Sign In button clicked", Toast.LENGTH_SHORT).show();

            Activity activity = getActivity();
            if (activity instanceof FragmentReplacerActivity) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new SignIn());
            }
            else {
                Log.d("not instanceof FragmentReplacerActivity", "" + getActivity());

            }

        }
        else{
            Toast.makeText(getContext(), "Sign In failed", Toast.LENGTH_SHORT).show();

        }
    }
}
package com.example.socialmediaapp.fragments;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.FragmentReplacerActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.SplashActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.example.socialmediaapp.fragments.SignIn;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Pattern;

public class SignIn extends Fragment {
    private EditText emailEt, passwordEt;
    private Button signInbtn;
    private ImageButton GGsignInbtn, FBsignInbtn;
    private TextView forgotPWTx, signUpTv;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN =1;
    private boolean showOneTapUI = true;
    GoogleSignInClient mGoogleSignInClient;

    public static FirebaseAuth auth;
    public static FirebaseUser user;
//    private LoginButton FBsignInbtn2;

    public ImageButton getFacebookSignInButton() {
        return FBsignInbtn;
    }
    public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    public SignIn() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
//        updateUI(currentUser);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        Log.d("onCreateView", "signin");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
//        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.default_web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                .build();

        // Initialize Google SignInClient
//        oneTapClient = GoogleSignIn.getClient(requireContext(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);

//        FBsignInbtn2.setReadPermissions("email", "public_profile");
        clickListener();

    }
    private void init(View v){
        emailEt= v.findViewById(R.id.emailET);
        passwordEt = v.findViewById(R.id.passwordET);
        signInbtn = v.findViewById(R.id.signinBtn);
        forgotPWTx = v.findViewById(R.id.forgotTV);
        signUpTv = v.findViewById(R.id.signupBtn);
        GGsignInbtn = v.findViewById(R.id.signupGG);
//        FBsignInbtn = v.findViewById(R.id.signupFB);

//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

        progressBar = v.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }
    private void clickListener(){
        signUpTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                loadFragment(new SignUp(), true);
                ((FragmentReplacerActivity) getActivity()).setFragment(new SignUp());
            }
        });
        forgotPWTx.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                loadFragment(new SignUp(), true);
                ((FragmentReplacerActivity) getActivity()).setFragment(new SendEmail());
            }
        });
        signInbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Processing", Toast.LENGTH_SHORT).show();

                String mail = emailEt.getText().toString();
                String pw = passwordEt.getText().toString();

                if (mail.isEmpty() || !pattern.matcher(mail).matches()){
                    emailEt.setError("Email is invalid");
                    return;
                }
                if (pw.isEmpty() || pw.length() <6){
                    passwordEt.setError("Password is invalid");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(mail,pw)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (!user.isEmailVerified()){
                                        Toast.makeText(getContext(),"Please verify your email", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);

                                    }
                                    gotoHomePageByReplacer();

                                }
                                else{
                                    String ext = "Error: " + task.getException().getMessage();
                                    Toast.makeText(getContext(),"ERROR", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        GGsignInbtn.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
//                Toast.makeText(getContext(), "GGclicked", Toast.LENGTH_SHORT).show();
                Google_signIn();
            }
        });
//        FBsignInbtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                Intent intent = new Intent(getActivity(), FBAuthActivity.class);
////                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
////                startActivity(intent);
//            }
//        });

    }
    private void gotoHomePageByMain (){
        if (getActivity()==null)
            return;

        progressBar.setVisibility(View.GONE);

        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }
    private void gotoHomePageByReplacer (){
        if (getContext() != null && getActivity() != null) {
//            Log.d("SigninFragment", "uploadUser: User data uploaded successfully");
//                    Toast.makeText(getContext(), "Sign In button clicked", Toast.LENGTH_SHORT).show();

            Activity activity = getActivity();
            progressBar.setVisibility(View.GONE);
            if (activity instanceof FragmentReplacerActivity) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                ((FragmentReplacerActivity) getActivity()).setFragment(new Home());
            }
            else {
                Log.d("not instanceof FragmentReplacerActivity", "" + getActivity());

            }

        }
        else{
            Toast.makeText(getContext(), "cant gotoHomePage", Toast.LENGTH_SHORT).show();

        }
    }

    private void Google_signIn (){
        Intent signInIntend = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntend,RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                Log.d("onActivityResult", acc.getId());
                assert acc != null;
                firebaseAuthWithGoogle(acc.getIdToken());

            }catch (ApiException e){
                Log.w("onActivityResult", "failed"+e);
                e.printStackTrace();
            }
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        }
                        else{
                            Log.d("firebaseAuthWithGoogle", "failed");

                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(getActivity());

        Map<String, Object> map = new HashMap<>();
        map.put("name",acc.getDisplayName());
        map.put("email",acc.getEmail());
        map.put("profileImg",String.valueOf(acc.getPhotoUrl()));
        map.put("uID",user.getUid());
        map.put("followers",0);
        map.put("following",0);


        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Sign in successful", Toast.LENGTH_SHORT).show();
                            gotoHomePageByReplacer();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Pass the activity result back to the Facebook SDK
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//    private void handleFacebookAccessToken(AccessToken token) {
//        Log.d("FB", "handleFacebookAccessToken:" + token);
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("FB", "signInWithCredential:success");
//                            FirebaseUser user = auth.getCurrentUser();
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("FB", "signInWithCredential:failure", task.getException());
////                            Toast.makeText(getContext(), "Authentication failed.",
////                                    Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                        }
//                    }
//                });
//    }
}
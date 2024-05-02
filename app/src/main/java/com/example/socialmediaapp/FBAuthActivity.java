package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.socialmediaapp.fragments.SignIn;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBAuthActivity extends AppCompatActivity {
//    CallbackManager callbackManager;
//    private LoginButton FBsignInbtn;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mAuth = FirebaseAuth.getInstance();
//
//        SignIn signInFragment = (SignIn) getSupportFragmentManager().findFragmentById(R.id.fragment_sign_in);
//        FBsignInbtn = signInFragment.getFacebookSignInButton();
//        callbackManager = CallbackManager.Factory.create();
//
//        FBsignInbtn.setReadPermissions("email", "public_profile");
//        LoginManager.getInstance().registerCallback(callbackManager,
//                new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        handleFacebookAccessToken(loginResult.getAccessToken());
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        // App code
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        // App code
//                    }
//                });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Pass the activity result back to the Facebook SDK
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//    private void handleFacebookAccessToken(AccessToken token) {
//        Log.d("TAG", "handleFacebookAccessToken:" + token);
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("TAG", "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("TAG", "signInWithCredential:failure", task.getException());
//                            Toast.makeText(FBAuthActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
//                        }
//                    }
//                });
//    }

//    private void updateUI(FirebaseUser user) {
//        Intent intent = new Intent(FBAuthActivity.this, FBAuthActivity.class);
//        startActivity(intent);
//    }
}
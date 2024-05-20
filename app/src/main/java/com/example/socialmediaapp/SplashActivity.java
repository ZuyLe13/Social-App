package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, FragmentReplacerActivity.class));
                //!!!!!!!!!!!!!!!CHỖ NÀY SỬA SAU
//                if (user==null){
//                    Log.d("SplashActivity", "FragmentReplacerActivity");
//                    startActivity(new Intent(SplashActivity.this, FragmentReplacerActivity.class));
//                }
//                else{
//                    Log.d("SplashActivity", "MainActivity");
//                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                }
                finish();
            }
        }, 3000);

    }
}
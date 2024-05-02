package com.example.socialmediaapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.socialmediaapp.fragments.SignIn;
import com.example.socialmediaapp.fragments.SignUp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;
//    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sign_up);

        loadFragment(new SignUp(), true); // Thêm SignUp Fragment vào activity


//        bottomNavView = findViewById(R.id.bottomNavView);
//
//        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//                int itemId = menuItem.getItemId();
//
//                if (itemId == R.id.navHome) {
//                    loadFragment(new Home(), false);
//                } else if (itemId == R.id.navSearch) {
//                    loadFragment(new Search(), false);
//                } else if (itemId == R.id.navNotification) {
//                    loadFragment(new Notification(), false);
//                } else {
//                    loadFragment(new Profile(), false);
//                }
//
//                return true;
//            }
//        });
//
//        loadFragment(new Home(), true);

    }

    private void loadFragment(Fragment frag, boolean isAppInitialized) {

        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();

        if (isAppInitialized) {
            fragTransaction.add(R.id.frameLayout, frag);
        } else {
            fragTransaction.replace(R.id.frameLayout, frag);
        }

        fragTransaction.commit();

    }
}
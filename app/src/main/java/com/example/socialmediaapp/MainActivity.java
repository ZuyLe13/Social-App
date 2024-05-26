package com.example.socialmediaapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Menu;
import com.example.socialmediaapp.fragments.SignIn;
import com.example.socialmediaapp.fragments.SignUp;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.view.MenuItem;
import android.widget.FrameLayout;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.socialmediaapp.fragments.Add;
import com.example.socialmediaapp.fragments.Home;
import com.example.socialmediaapp.fragments.Profile;
import com.example.socialmediaapp.fragments.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.socialmediaapp.fragments.Search;
import com.example.socialmediaapp.fragments.Notification;
import com.google.firebase.auth.FirebaseAuth;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements Search.OnDataPass {

    private BottomNavigationView bottomNavView;
    private FrameLayout frameLayout;
    public static String currentUid;
    public static Boolean isSearching = false;
    public static String MyName;

    public MainActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.navHome) {
                    loadFragment(new Home(), false);
                } else if (itemId == R.id.navSearch) {
                    loadFragment(new Search(), false);
                } else if (itemId == R.id.navAddPost) {
                    loadFragment(new Add(), false);
                } else if (itemId == R.id.navNotification) {
                    loadFragment(new Notification(), false);
                } else {
                    isSearching = false;
                    loadFragment(new Profile(), false);
                }

                return true;
            }
        });

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadFragment(new Home(), true);

    }

    private void loadFragment(Fragment frag, boolean isAppInitialized) {

        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();

        if (isAppInitialized) {
            fragTransaction.add(frameLayout.getId(), frag);
        } else {
            fragTransaction.replace(frameLayout.getId(), frag);
        }

        fragTransaction.commit();

    }



    @Override
    public void onChange(String uID) {
        currentUid = uID;
        isSearching = true;
        loadFragment(new Profile(), false);
    }

    @Override
    public void onBackPressed(){
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment instanceof Profile){
            isSearching = false;
            loadFragment(new Home(), false);
        } else {
            super.onBackPressed();
        }
    }
}
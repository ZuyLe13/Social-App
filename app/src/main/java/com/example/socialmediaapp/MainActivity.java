package com.example.socialmediaapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.socialmediaapp.fragments.Home;
import com.example.socialmediaapp.fragments.Search;
import com.example.socialmediaapp.fragments.Notification;
import com.example.socialmediaapp.fragments.Profile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;
//    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavView = findViewById(R.id.bottomNavView);
//        frameLayout = findViewById(R.id.frameLayout);

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.navHome) {
                    loadFragment(new Home(), false);
                } else if (itemId == R.id.navSearch) {
                    loadFragment(new Search(), false);
                } else if (itemId == R.id.navNotification) {
                    loadFragment(new Notification(), false);
                } else {
                    loadFragment(new Profile(), false);
                }

                return true;
            }
        });

        loadFragment(new Home(), true);

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
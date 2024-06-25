package com.example.socialmediaapp;

import static com.example.socialmediaapp.utils.Constants.PREF_DIRECTORY;
import static com.example.socialmediaapp.utils.Constants.PREF_NAME;
import static com.example.socialmediaapp.utils.Constants.PREF_STORED;
import static com.example.socialmediaapp.utils.Constants.PREF_URL;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Menu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.socialmediaapp.fragments.SignIn;
import com.example.socialmediaapp.fragments.SignUp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Search.OnDataPass, Profile.OnDataPassFriend {

    private BottomNavigationView bottomNavView;
    private FrameLayout frameLayout;
    public static String currentUid;
    public static Boolean isSearching = false;
    public static String MyName;

    FirebaseUser firebaseUser;
    DatabaseReference activeRef;


    public MainActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frameLayout);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        activeRef = FirebaseDatabase.getInstance().getReference("UserActive").child(firebaseUser.getUid());






//        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (value == null || !value.exists()) return;
//                String profileURL = value.getString("profileImg");
//                Log.d("TEST !!!", "profile URL: " + profileURL);
//                if (profileURL != null) {
//                    Glide.with(MainActivity.this)
//                            .asDrawable()
//                            .load(profileURL)
//                            .placeholder(R.drawable.profile)
//                            .circleCrop()
//                            .timeout(6500)
//                            .into(new CustomTarget<Drawable>() {
//                                @Override
//                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
//                                    storeProfileImage(bitmap, profileURL);
//                                }
//
//                                @Override
//                                public void onLoadCleared(@Nullable Drawable placeholder) {
//                                }
//                            });
//                }
//            }
//        });
//
//        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
//        String directory = preferences.getString(PREF_DIRECTORY, "");
//        Bitmap bitmap = loadProfileImage(directory);
//        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
//        bottomNavView.getMenu().findItem(R.id.navProfile).setIcon(drawable);


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

    private void active(String status) {
        if (firebaseUser != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("active", status);
            hashMap.put("uID", firebaseUser.getUid()); // Thêm uID vào bản ghi trong Realtime Database

            activeRef.setValue(hashMap);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        active("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        active("offline");
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




//    private void storeProfileImage(Bitmap bitmap, String url){
//        SharedPreferences preferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        boolean isStored = preferences.getBoolean(PREF_STORED, false);
//        String urlString = preferences.getString(PREF_URL, "");
//        SharedPreferences.Editor editor = preferences.edit();
//
//        if (isStored && urlString.equals(url)) return;
//        if (isSearching) return;
//
//        ContextWrapper wrapper = new ContextWrapper(this.getApplicationContext());
//        File directory = wrapper.getDir("image_data", Context.MODE_PRIVATE);
//
//        if (!directory.exists()) directory.mkdir();
//
//        File path = new File(directory, "profile.png");
//        FileOutputStream outputStream = null;
//
//        try {
//            outputStream = new FileOutputStream(path);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//        } catch (FileNotFoundException e){
//            e.printStackTrace();
//        } finally {
//            try{
//                assert outputStream != null;
//                outputStream.close();
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//        }
//
//        editor.putBoolean(PREF_STORED, true);
//        editor.putString(PREF_URL, url);
//        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
//        editor.apply();
//    }
//
//    private Bitmap loadProfileImage(String directory){
//        try {
//            File file = new File(directory, "profile.png");
//            return BitmapFactory.decodeStream(new FileInputStream(file));
//        } catch (FileNotFoundException e){
//            e.printStackTrace();
//            return null;
//        }
//    }
}
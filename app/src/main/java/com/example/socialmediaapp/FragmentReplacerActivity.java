package com.example.socialmediaapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialmediaapp.fragments.Collection;
import com.example.socialmediaapp.fragments.Comment;
import com.example.socialmediaapp.fragments.Profile;
import com.example.socialmediaapp.fragments.SignIn;
import com.example.socialmediaapp.fragments.SignUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FragmentReplacerActivity extends AppCompatActivity implements Profile.OnDataPassFriend {

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fragment_replacer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        frameLayout = findViewById(R.id.TframeLayout);

        // Kiểm tra trạng thái đăng nhập của người dùng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            setFragment(new SignIn());
            return;
        }

        String FragmentType = getIntent().getStringExtra("FragmentType");
        if (FragmentType != null) {
            switch (FragmentType) {
                case "Comment":
                    setFragment(new Comment());
                    break;
                case "Profile":
                    String profileUID = getIntent().getStringExtra("profileUID");
                    Profile profileFragment = new Profile();
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", profileUID);
                    bundle.putString("currentUID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    profileFragment.setArguments(bundle);
                    setFragment(profileFragment);
                    break;
                case "Collection":
                    setFragment(new Collection());
                    break;
                default:
                    setFragment(new SignIn());
                    break;
            }
        } else {
            setFragment(new SignIn());
        }
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
        fragTrans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment instanceof SignUp) {
            fragTrans.addToBackStack(null);
        }
        if (fragment instanceof Comment) {
            String id = getIntent().getStringExtra("id");
            String uid = getIntent().getStringExtra("uid");
            String currentUID = getIntent().getStringExtra("currentUID");

            Log.d("FragmentReplacerActivity", "ID: " + id + ", UID: " + uid + ", currentUID: " + currentUID);

            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("uid", uid);
            bundle.putString("currentUID", currentUID);
            fragment.setArguments(bundle);
        }
        if (fragment instanceof Profile) {
            String uid = getIntent().getStringExtra("uid");
            String currentUID = getIntent().getStringExtra("currentUID");

            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            bundle.putString("currentUID", currentUID);
            fragment.setArguments(bundle);
        }
        if (fragment instanceof Collection) {
            String collectionID = getIntent().getStringExtra("collectionID");
            String collectionName = getIntent().getStringExtra("collectionName");
            String collectionUID = getIntent().getStringExtra("collectionUID");

            Bundle bundle = new Bundle();
            bundle.putString("collectionID", collectionID);
            bundle.putString("collectionName", collectionName);
            bundle.putString("collectionUID", collectionUID);
            fragment.setArguments(bundle);
        }

        fragTrans.replace(frameLayout.getId(), fragment);
        fragTrans.commit();
    }

    @Override
    public void onChange(String uID) {
        // Triển khai phương thức onChange, bạn có thể để trống hoặc thêm logic nếu cần
        Log.d("FragmentReplacerActivity", "onChange called with uID: " + uID);
    }
}

package com.example.socialmediaapp;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialmediaapp.fragments.SignIn;
import com.example.socialmediaapp.fragments.SignUp;

public class FragmentReplacerActivity extends AppCompatActivity {

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
        setFragment(new SignIn());
    }
    public void setFragment(Fragment fragment){
        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
        fragTrans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment instanceof SignUp){
            fragTrans.addToBackStack(null);
        }
        fragTrans.replace(frameLayout.getId(),fragment);
        fragTrans.commit();
    }
}
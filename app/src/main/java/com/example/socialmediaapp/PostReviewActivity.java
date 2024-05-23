package com.example.socialmediaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.net.MalformedURLException;
import java.net.URL;

public class PostReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_review);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();
        String query = uri.getQuery();


//            URL url = new URL(scheme + "://" + host + path.replace("Post Images", "Post%20Images") + "?" + query);

            FirebaseStorage.getInstance().getReference().child(uri.getLastPathSegment())
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ImageView shareLinkIV = findViewById(R.id.shareLinkIV);

                            Glide.with(PostReviewActivity.this)
                                    .load(uri.toString())
                                    .timeout(6500)
                                    .into(shareLinkIV);
                        }
                    });

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(PostReviewActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(PostReviewActivity.this, FragmentReplacerActivity.class));
        }
    }
}
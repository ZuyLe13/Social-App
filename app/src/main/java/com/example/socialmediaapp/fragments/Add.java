package com.example.socialmediaapp.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.socialmediaapp.MainActivity.MyName;
import static com.example.socialmediaapp.utils.ImageContent.loadSavedImages;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.GalleryAdapter;
import com.example.socialmediaapp.model.GalleryImageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Add extends Fragment {
    private EditText postDescriptionEditText;
    private ImageView postImageView;
    private RecyclerView postRecyclerView;
    private ImageButton backBtn, nextBtn;
    private List<GalleryImageModel> galleryList;
    private GalleryAdapter galleryAdapter;
    private Uri imageUri;
    private String imageURL;
    private FirebaseUser user;
    Dialog dialog;
    ActivityResultLauncher<Intent> resultLauncher;
    public Add() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        init(view);
        postRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postRecyclerView.setHasFixedSize(true);

        galleryList = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(galleryList);
        postRecyclerView.setAdapter(galleryAdapter);

        registerResult();
        pickImage();
        clickListener();

    }

    private void clickListener(){
        galleryAdapter.SendImage(new GalleryAdapter.SendImage() {
            @Override
            public void onSend(Uri picUri) {

                CropImage.activity(picUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(4, 3)
                        .start(getContext(), Add.this);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());
                dialog.show();

                storageReference.putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageURL = uri.toString();
                                            uploadData(imageURL);
                                        }
                                    });
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "Failed to upload post", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void registerResult(){
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            imageUri = o.getData().getData();
                            postImageView.setImageURI(imageUri);
                            postImageView.setVisibility(View.VISIBLE);
                            nextBtn.setVisibility(View.VISIBLE);
                            Log.d("TEST !!!", "Image URI: " + imageUri);
                        } catch (Exception e){
                            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void pickImage(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void uploadData(String imageURL){

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).collection("Post Images");

        String id = reference.document().getId();
        String description = postDescriptionEditText.getText().toString();
        List<String> reactList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("description", description);
        map.put("imageUrl", imageURL);
        map.put("timeStamp", FieldValue.serverTimestamp());

        map.put("name", MyName);
        map.put("profileImage", String.valueOf(user.getPhotoUrl()));
        map.put("reacts", reactList);
        map.put("comments", "");
        map.put("uid", user.getUid());

        reference.document(id).set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            System.out.println("Post Successfully !");
                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    private void init(View view){
        postDescriptionEditText = view.findViewById(R.id.postDescriptionEditText);
        postImageView = view.findViewById(R.id.postImageView);
        postRecyclerView = view.findViewById(R.id.postRecycleView);
        backBtn = view.findViewById(R.id.postBackBtn);
        nextBtn = view.findViewById(R.id.postNextBtn);
        user = FirebaseAuth.getInstance().getCurrentUser();

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_bg, null));
        dialog.setCancelable(false);
    }

    @Override
    public void onResume(){
        super.onResume();



//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                Log.d("TEST !!!", "before get permission in onResume and get path");
//                Dexter.withContext(getContext())
//                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.MANAGE_EXTERNAL_STORAGE
//                                )
//                        .withListener(new MultiplePermissionsListener() {
//                            @Override
//                            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                                if (report.areAllPermissionsGranted()){
//                                    Log.d("TEST !!!", "hello 1");
////                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
//                                    File file = new File(Environment.DIRECTORY_DOWNLOADS);
//                                    if (file.exists()){
//                                        File[] files = file.listFiles();
//
//                                        assert files != null;
//                                        for (File fileitem : files){
//                                            if (fileitem.getAbsolutePath().endsWith(".jpg") ||
//                                                    fileitem.getAbsolutePath().endsWith(".png")){
//                                                galleryList.add(new GalleryImageModel(Uri.fromFile(fileitem)));
//                                                galleryAdapter.notifyDataSetChanged();
//                                            }
//                                        }
//
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                                Log.d("TEST !!!", "Permission rationale should be shown");
//                                permissionToken.continuePermissionRequest();
//                            }
//                        });
//            }
//        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (requestCode == RESULT_OK){
//
//                imageUri = result.getUri();
//
//                Glide.with(getContext())
//                        .load(imageUri)
//                        .into(postImageView);
//                postImageView.setVisibility(View.VISIBLE);
//                nextBtn.setVisibility(View.VISIBLE);
//            }
//        }
//    }
}
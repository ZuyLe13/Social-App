package com.example.socialmediaapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder>{

    private List<UserModel> userModelList;
    private OnProfileChosen onProfileChosen;
    private int layoutID;

    public UserAdapter(List<UserModel> userModelList, int layoutID) {
        this.userModelList = userModelList;
        this.layoutID = layoutID;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.layoutID, parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, @SuppressLint("RecyclerView") int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//        if (userModelList.get(position).getuID().equals(user.getUid())){
//            holder.searchRL.setVisibility(View.GONE);
//        }

        holder.searchNameTV.setText(userModelList.get(position).getName());
        holder.searchStatusTV.setText(userModelList.get(position).getStatus());

        Glide.with(holder.itemView.getContext().getApplicationContext())
                .load(userModelList.get(position).getProfileImg())
                .placeholder(R.drawable.ic_avt)
                .timeout(6500)
                .into(holder.searchProfileImageCIV);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onProfileChosen.onChosen(userModelList.get(position).getuID());
                }
            });

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder{

        private CircleImageView searchProfileImageCIV;
        private TextView searchNameTV, searchStatusTV;
        private RelativeLayout searchRL;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            searchProfileImageCIV = itemView.findViewById(R.id.searchProfileImageCIV);
            searchNameTV = itemView.findViewById(R.id.searchNameTV);
            searchStatusTV = itemView.findViewById(R.id.searchStatusTV);
            searchRL = itemView.findViewById(R.id.searchRL);
            searchNameTV.setSelected(true);
            searchStatusTV.setSelected(true);
        }

    }

    public void OnProfileChosen (OnProfileChosen onProfileChosen){
        this.onProfileChosen = onProfileChosen;
    }

    public interface OnProfileChosen{
        void onChosen(String uID);
    }
}

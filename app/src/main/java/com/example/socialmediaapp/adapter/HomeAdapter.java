package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.fragments.Home;
import com.example.socialmediaapp.model.HomeModel;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    private List<HomeModel> list;
    private Context context;
    private OnPressed onPressed;

    public HomeAdapter(List<HomeModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);

        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        holder.userNameTV.setText(list.get(position).getName());
        holder.timeStampTV.setText("" + list.get(position).getTimeStamp());

        List<String> reactList = list.get(position).getReactList();
//        if (reactList.isEmpty()) {
//            holder.reactCountTV.setVisibility(View.INVISIBLE);
//        } else {
//            holder.reactCountTV.setText(String.valueOf(reactList.size()));
//        }

//        int commentCount = list.get(position).getCommentCount();
//        if (commentCount == 0) {
//            holder.commentCountTV.setVisibility(View.INVISIBLE);
//        } else {
//            holder.commentCountTV.setText(String.valueOf(commentCount));
//        }

        holder.descriptionTV.setText(list.get(position).getDescription());

        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.ic_avt)
                .timeout(6500)
                .into(holder.profileImage);

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        holder.clickListener(position, list.get(position).getId(), list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HomeHolder extends RecyclerView.ViewHolder{

        private CircleImageView profileImage;
        private TextView userNameTV, timeStampTV, commentCountTV, reactCountTV, descriptionTV;
        private ImageView imageView;
        private ImageButton reactBtn, commentBtn, shareBtn;
        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.userAvt);
            imageView = itemView.findViewById(R.id.postImageView);
            userNameTV = itemView.findViewById(R.id.userName);
            timeStampTV = itemView.findViewById(R.id.timeStampTextView);
            reactCountTV = itemView.findViewById(R.id.reactCountTextView);
            commentCountTV = itemView.findViewById(R.id.commentCountTextView);
            reactBtn = itemView.findViewById(R.id.reactBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTV = itemView.findViewById(R.id.desciptionTextView);

        }

        public void clickListener(int position, String id, String name) {
            reactBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPressed.onReacted(position, id);
                }
            });
        }
    }

    public interface OnPressed{
        void onReacted (int position, String id);
        void onComment(int position, String id, String comment);
    }
    public void OnPressed (OnPressed onPressed){
        this.onPressed = onPressed;
    }

}

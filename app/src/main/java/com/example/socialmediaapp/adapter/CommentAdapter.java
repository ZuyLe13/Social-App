package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.model.CommentModel;
import com.example.socialmediaapp.utils.TimeUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private Context context;
    private List<CommentModel> commentList;

    public CommentAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent,false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Glide.with(context)
                .load(commentList.get(position).getAvt())
                        .into(holder.userAvtCIV);

        holder.userNameTV.setText(commentList.get(position).getName());
        holder.commentTV.setText(commentList.get(position).getComment());
        holder.timeTV.setText(TimeUtils.getTimeAgo(commentList.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentHolder extends RecyclerView.ViewHolder{
        CircleImageView userAvtCIV;
        TextView userNameTV, commentTV, timeTV;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);

            userAvtCIV = itemView.findViewById(R.id.commentedUserAvt);
            userNameTV = itemView.findViewById(R.id.commentedUserName);
            commentTV = itemView.findViewById(R.id.commentedCommentTV);
            timeTV = itemView.findViewById(R.id.commentedTimeTV);

        }
    }
}

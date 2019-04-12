package com.patternjkh.ui.apps;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Comment;

import java.util.List;

public class CommentsAdapterCons extends RecyclerView.Adapter<CommentsAdapterCons.CommentsViewHolder> {

    private List<Comment> comments;

    private static final int COMMENT_AUTHOR = 0;
    private static final int COMMENT_NOT_AUTHOR = 1;

    public CommentsAdapterCons(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommentsViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case COMMENT_AUTHOR:
                View itemViewAuthor = inflater.inflate(R.layout.item_comment, parent, false);
                viewHolder = new CommentsViewHolder(itemViewAuthor);
                break;

            case COMMENT_NOT_AUTHOR:
                View itemView = inflater.inflate(R.layout.item_comment_cons, parent, false);
                viewHolder = new CommentsViewHolder(itemView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.com_date.setText(comment.getOwner());
        holder.com_user.setText(comment.getName());
        holder.com_text.setText(comment.getText());
        if (comment.getHidden()) {
            holder.com_text.setTextColor(holder.com_text.getContext().getResources().getColor(R.color.comment_hidden_gray));
        }
    }

    @Override
    public int getItemCount() {
        if (comments == null) {
            return 0;
        }
        return comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (comments.get(position).getAuthor()) ? COMMENT_AUTHOR : COMMENT_NOT_AUTHOR;
    }

    public void add(Comment comment) {

        comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    class CommentsViewHolder extends RecyclerView.ViewHolder {
        private TextView com_date, com_user, com_text;

        CommentsViewHolder(View itemView) {
            super(itemView);

            com_date = itemView.findViewById(R.id.comment_date);
            com_user = itemView.findViewById(R.id.comment_user);
            com_text = itemView.findViewById(R.id.comment_text);
        }
    }
}
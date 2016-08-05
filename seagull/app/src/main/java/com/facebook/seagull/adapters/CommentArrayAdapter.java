package com.facebook.seagull.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.seagull.R;
import com.facebook.seagull.TimeFormatter;
import com.facebook.seagull.activities.HomeActivity;
import com.facebook.seagull.activities.ProfileActivity;
import com.facebook.seagull.models.Comment;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klimjinx on 7/29/16.
 */

public class CommentArrayAdapter extends RecyclerView.Adapter<CommentArrayAdapter.ViewHolder> {
    // Store a member variable for the comments
    private ArrayList<Comment> amComments;
    // Store the context for easy access
    private Context mContext;
    // For customizing font
    private Typeface font;

    // Pass in the routs array into the constructor
    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        this.amComments = comments;
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfile;
        private TextView tvName;
        private TextView tvBody;
        private TextView tvTimestamp;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CommentArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView;
        contactView = inflater.inflate(R.layout.item_comment, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final CommentArrayAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Comment comment = amComments.get(position);
        final int pos = position;

        final ParseUser user = comment.getUser();

        final TextView tvName = viewHolder.tvName;
        tvName.setText(comment.getUser().getString("first_name") + " " + comment.getUser().getString("last_name"));

        final TextView tvTimestamp = viewHolder.tvTimestamp;
        tvTimestamp.setText(TimeFormatter.getTimeDifference(comment.getCreatedAt().toString()));

        final TextView tvBody = viewHolder.tvBody;
        tvBody.setText(comment.getMessage());

        ImageView ivProfile = viewHolder.ivProfile;
        Glide.with(mContext).load(comment.getUser().getString("picture")).into(ivProfile);

        viewHolder.ivProfile.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        Intent i = new Intent(mContext, HomeActivity.class);
                        i.putExtra("profile", "personal");
                        mContext.startActivity(i);
                    } else{
                        Intent i = new Intent(mContext, ProfileActivity.class);
                        i.putExtra("id", user.getObjectId());
                        mContext.startActivity(i);
                    }

                }
            }
        );

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return amComments.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        amComments.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Comment> list) {
        amComments.addAll(list);
        notifyDataSetChanged();
    }
}

package com.facebook.seagull.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.seagull.R;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by satchinc on 7/26/16.
 */
public class InvitedFriendListAdapter extends RecyclerView.Adapter<InvitedFriendListAdapter.ViewHolder> {

    private ArrayList<ParseUser> afriendChecklist;
    private ParseUser owner;
    private Context mContext;

    public InvitedFriendListAdapter(Context context, ArrayList<ParseUser> objects) {
        this.afriendChecklist = objects;
        this.mContext = context;

    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        private ImageView ivProfile;
        private TextView tvName;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }

    public void setOwner(ParseUser owner) {
        this.owner = owner;
    }

    @Override
    public InvitedFriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.friend_item_no_checkbox, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final  InvitedFriendListAdapter.ViewHolder holder, int position) {
        final ParseUser friend = afriendChecklist.get(position);

        String name = "";
        if (friend.equals(owner)){
            name = friend.getString("first_name") + " " + friend.getString("last_name") + " (Owner)";
        } else{
            name = friend.getString("first_name") + " " + friend.getString("last_name");
        }

        String profile = friend.getString("picture");
        holder.ivProfile.setImageResource(0);

        if (!TextUtils.isEmpty(profile)) {
            Picasso.with(mContext).load(profile).into( holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(0);
        }

        holder.tvName.setText(name);

    }

    @Override
    public int getItemCount() {
        return afriendChecklist.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        afriendChecklist.clear();
        notifyDataSetChanged();
    }

}

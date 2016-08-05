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
import com.facebook.seagull.activities.ProfileActivity;
import com.facebook.seagull.models.Party;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klimjinx on 7/26/16.
 */

public class PartiesArrayAdapter extends RecyclerView.Adapter<PartiesArrayAdapter.ViewHolder> {
    // Store a member variable for the parties
    private ArrayList<Party> amParties;
    // Store the context for easy access
    private Context mContext;
    // For customizing font
    private Typeface font;

    // Pass in the routs array into the constructor
    public PartiesArrayAdapter(Context context, ArrayList<Party> parties) {
        this.amParties = parties;
        this.mContext = context;
//            font = Typeface.createFromAsset(context.getAssets(), "fonts/GothamNarrow-Book.otf");
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        private ImageView ivPartyCover;
        private ImageView ivHost;
        private TextView tvHost;
        private TextView tvDate;
        private TextView tvName;
        private TextView tvTime;

        private TextView tvLocation;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

//                tvUsername.setTypeface(font);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivPartyCover = (ImageView) itemView.findViewById(R.id.ivPartyCover);
            ivHost = (ImageView) itemView.findViewById(R.id.ivHost);
            tvHost = (TextView) itemView.findViewById(R.id.tvHost);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
//            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public PartiesArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_party, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final PartiesArrayAdapter.ViewHolder viewHolder, int position) {

        // Get the data model based on position
        final Party party = amParties.get(position);
        final int pos = position;

        final TextView tvDate = viewHolder.tvDate;
        final TextView tvTime = viewHolder.tvTime;

        if (party.getDate() != null) {
            tvDate.setText(party.getDate());
        }
//        tvLocation.setText(party.getRoute().getParseGeoPoint());

        // Set hosted by
        final ImageView ivHost = viewHolder.ivHost;
        final TextView tvHost = viewHolder.tvHost;
        // Default values if no user associated with Party
        tvHost.setText(mContext.getString(R.string.no_user));
        ivHost.setImageResource(0);
        final ParseUser user = party.getOwner();
        if (user != null) {
            Glide.with(mContext).load(user.getString("picture")).into(ivHost);
            String hostText = user.getString("first_name");
            tvHost.setText(hostText);
        }

        // Set party name and cover pic
        final TextView tvName = viewHolder.tvName;
        tvName.setText(party.getName());
        final ImageView ivPartyCover = viewHolder.ivPartyCover;
        String picUrl = null;
        picUrl = (String) party.getRoute().get("photoUrl");
        Glide.with(mContext).load(picUrl).into(ivPartyCover);

        tvTime.setText(party.getTime());

        ivHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ProfileActivity.class);

                if (user != ParseUser.getCurrentUser()) {
                    // other use so pass in ID (parse)
                    i.putExtra("id", user.getObjectId());
                    mContext.startActivity(i);
                } else {
                    // personal profile
                    mContext.startActivity(i);
                }
            }
        });

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return amParties.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        amParties.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Party> list) {
        amParties.addAll(list);
        notifyDataSetChanged();
    }

}

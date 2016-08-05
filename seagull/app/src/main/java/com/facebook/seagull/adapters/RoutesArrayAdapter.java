package com.facebook.seagull.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.seagull.R;
import com.facebook.seagull.listeners.LikeListener;
import com.facebook.seagull.models.Route;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/*
* RVAdapter for Routes. Find and Set Views here
* */

public class RoutesArrayAdapter extends RecyclerView.Adapter<RoutesArrayAdapter.ViewHolder> {
    // Store a member variable for the routes
    private ArrayList<Route> amRoutes;
    // Store the context for easy access
    private Context mContext;

    private Boolean profile;
    // For customizing font
    private Typeface font;

    private onUserClickListener listener;

    private final double DEFAULT_ASPECT_RATIO = 1.0;
    private final double MAX_ASPECT_RATIO = 2.0;
    private final double MIN_ASPECT_RATIO = 0.7;
    private final int DEFAULT_WIDTH = 500;


    static final String STATIC_MAP_BASE = "https://maps.googleapis.com/maps/api/staticmap";

    // Pass in the routs array into the constructor
    public RoutesArrayAdapter(Context context, ArrayList<Route> routes, Boolean profile) {
        this.amRoutes = routes;
        this.mContext = context;
        this.profile = profile;
//            font = Typeface.createFromAsset(context.getAssets(), "fonts/GothamNarrow-Book.otf");
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        // TODO customize item_route.xml
        private ImageView ivStaticMap;
        private ImageView btnLike;
        private ImageView ivUser;
        private TextView tvName;
        private TextView tvUserName;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

//                tvUsername.setTypeface(font);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivStaticMap = (ImageView) itemView.findViewById(R.id.ivStaticMap);
            btnLike = (ImageView) itemView.findViewById(R.id.btnLike);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RoutesArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView;
        if (profile) {
            contactView = inflater.inflate(R.layout.item_profile_route, parent, false);
        } else {
            // Inflate the custom layout
            contactView = inflater.inflate(R.layout.item_route, parent, false);
        }
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final RoutesArrayAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Route route = amRoutes.get(position);
        final int pos = position;
        // TODO Set item views based on your views and data model
        TextView tvName = viewHolder.tvName;
        tvName.setText(route.getName());

        ImageView ivStaticMap = viewHolder.ivStaticMap;
        Picasso.with(mContext).load(route.getPhotoUrl()).into(ivStaticMap);

        final ImageView btnLike = viewHolder.btnLike;

        // Default gray heart for unliked
        btnLike.setTag(false);
        btnLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heartgray, null));

        // Query Parse database to check if route was liked
        // Sets drawable accordingly
        LikeListener.queryLiked(mContext, route, btnLike);

        // Sets listener to like and unlike route and update database
        LikeListener likes = new LikeListener(mContext, btnLike, route, null);
        btnLike.setOnClickListener(likes);

        final ImageView ivUser = viewHolder.ivUser;
        final TextView tvUserName = viewHolder.tvUserName;

        // Default values if no user associated with Route
        tvUserName.setText(mContext.getString(R.string.no_user));
        ivUser.setImageResource(0);

        final ParseUser user = route.getUser();

        if (user != null) {
                Glide.with(mContext).load(user.getString("picture")).asBitmap().centerCrop().into(new BitmapImageViewTarget(ivUser) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivUser.setImageDrawable(circularBitmapDrawable);
                    }
                });
                tvUserName.setText(user.getString("first_name") + " " + user.getString("last_name"));
        }

        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoutesArrayAdapter.this.listener.onUserClick(user.getObjectId());
            }
        });

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoutesArrayAdapter.this.listener.onUserClick(user.getObjectId());
            }
        });
    }

    public interface onUserClickListener {
        public void onUserClick(String userId);
    }

    public void setOnUserClickListener(onUserClickListener instance) {
        this.listener = instance;
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return amRoutes.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        amRoutes.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Route> list) {
        amRoutes.addAll(list);
        notifyDataSetChanged();
    }
}

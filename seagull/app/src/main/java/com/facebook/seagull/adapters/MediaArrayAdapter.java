package com.facebook.seagull.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.seagull.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MediaArrayAdapter extends RecyclerView.Adapter<MediaArrayAdapter.ViewHolder> {
    // Store a member variable for the media
    private ArrayList<String> amMedia;
    // Store the context for easy access
    private Context mContext;
    // For customizing font
    private Typeface font;

    static final String STATIC_MAP_BASE = "https://maps.googleapis.com/maps/api/staticmap";

    // Pass in the routs array into the constructor
    public MediaArrayAdapter(Context context, ArrayList<String> media) {
        this.amMedia = media;
        this.mContext = context;
//            font = Typeface.createFromAsset(context.getAssets(), "fonts/GothamNarrow-Book.otf");
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        // TODO customize item_media.xml
        private ImageView ivMedia;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            // TODO findViewById according to item_media.xml
            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public MediaArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_media, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final MediaArrayAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final String mediaString = amMedia.get(position);
        final int pos = position;

        // TODO Set item views based on your views and data model
        ImageView ivMedia = viewHolder.ivMedia;
        ivMedia.setImageResource(0);
        Picasso.with(mContext).load(mediaString).into(ivMedia);

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return amMedia.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        amMedia.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<String> list) {
        amMedia.addAll(list);
        notifyDataSetChanged();
    }
}

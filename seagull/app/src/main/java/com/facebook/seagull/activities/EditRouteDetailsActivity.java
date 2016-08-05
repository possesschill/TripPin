package com.facebook.seagull.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.clients.LabelClient;
import com.facebook.seagull.clients.PanoramioClient;
import com.facebook.seagull.models.LWaypoint;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.LabelCheckBox;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.facebook.seagull.parsers.Bounds;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class EditRouteDetailsActivity extends AppCompatActivity {

    @BindView(R.id.etTitle) EditText etTitle;
    @BindView(R.id.etDescription) EditText etDescription;
    @BindView(R.id.ivStaticMap) ImageView ivStaticMap;
    @BindView(R.id.glNested) GridLayout glNested;
//    @BindView(R.id.llNested) LinearLayout llNested;
    @BindView(R.id.rvPhotos) RecyclerView rvPhotos;
//    @BindView(R.id.toolbar) Toolbar toolbar;

    static final String STATIC_MAP_BASE = "https://maps.googleapis.com/maps/api/staticmap";
    private PanoramioClient client = new PanoramioClient();
    private ArrayList<LWaypoint> lwaypointArray;
    private String encodedPolyline;
    private Bounds bounds;
    private double aspectRatio;
    private Route mRoute;
    private ArrayList<LabelCheckBox> labels;
    private ArrayList<String> mediaURL;
    private HorizontalPhotoAdapter horizontalAdapter;
    private ProgressDialog progressDialog;
    public int pad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_route_details);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        pad = (int) (5 * scale + 0.5f);

        ButterKnife.bind(this);
        LabelClient labelMaker = new LabelClient();
        labels = new ArrayList<>();
        labelMaker.queryAndLoadSearchOptions(labels, getApplicationContext(), glNested);
        // We can set the text that starts overlaid on the image and then turns into toolbar text
//        setSupportActionBar(mActionBarToolbar);
//        getSupportActionBar().setTitle(getResources().getString(R.string.edit_route_details));
        lwaypointArray = new ArrayList<>();
        mediaURL = new ArrayList<>();

        horizontalAdapter = new HorizontalPhotoAdapter(mediaURL);
        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(EditRouteDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        //horizontalLayoutManagaer.getFocusedChild();
        rvPhotos.setLayoutManager(horizontalLayoutManagaer);
        rvPhotos.setAdapter(horizontalAdapter);

        lwaypointArray= getIntent().getParcelableArrayListExtra("waypointArray");
        encodedPolyline = getIntent().getStringExtra("encodedPolyline");
        bounds = Parcels.unwrap(getIntent().getParcelableExtra("bounds"));
        aspectRatio = getIntent().getDoubleExtra("aspectRatio", 1);

        loadStaticMap();
        callForPhotos();
    }

    public void saveLabelsAndWaypoints(final Route route) {
        ParseRelation<Label> relation = route.getRelation("label");
        for (LabelCheckBox label : labels) {
            if (label.getCheckBox().isChecked()) {
                relation.add(label.getLabel());
            }
        }
        route.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Waypoint.fromArrayList(lwaypointArray, mRoute, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                progressDialog.dismiss();
                                Intent i = new Intent(EditRouteDetailsActivity.this, RouteDetailActivity.class);
                                i.putExtra("route_id", route.getObjectId());
                                startActivity(i);
                            }
                            else {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean checkLabeled(){
        for (LabelCheckBox label : labels) {
            if (label.getCheckBox().isChecked()) {
                return true;
            }
        }
        return false;
    }

    public void loadStaticMap() {
        // limit aspect ratio to prevent images from being too tall or too skinny
        if (aspectRatio > 4.0) {
            aspectRatio = 4.0;     // wide images
        }
        if (aspectRatio < 1.2) {
            aspectRatio = 1.2;     // tall images
        }

        // Calculate dimensions for static map
        int width = 620;     // max allowed for google api
        int height = (int) Math.round(width / aspectRatio) + 200;   // add 200 to avoid cutoff by status bar

        // Decrease width if height is maxed out
        if (height > 620) {
            height = 620;
            width = (int) Math.round(aspectRatio * (height - 200));
        }

        Log.d("STATIC_MAP", "API call - Width: " + width + " Height: " + height);

        // Scale static map dimensions to screen dimensions to size imageview
        int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = (int) Math.round((screenWidth * height) / (width * 1.0));

        // Dynamically set ImageView size before image loads
        CollapsingToolbarLayout.LayoutParams layoutParams = new CollapsingToolbarLayout.LayoutParams(screenWidth, screenHeight);
        ivStaticMap.setLayoutParams(layoutParams);

        Uri staticMapUri = Uri.parse(STATIC_MAP_BASE)
                .buildUpon()
                .appendQueryParameter("size", "" + width + "x" + height)
                .appendQueryParameter("path", "weight:6|color:blue|enc:" + encodedPolyline)
                .appendQueryParameter("key", getResources().getString(R.string.google_maps_key))
                .build();

        Log.d("STATIC_MAP", "url: " + staticMapUri.toString());

        Picasso.with(getApplicationContext()).load(staticMapUri.toString()).into(ivStaticMap);

        // Add static map to list of images to choose as route picture
        // Insert at first position
        mediaURL.add(0, staticMapUri.toString());
        horizontalAdapter.notifyItemInserted(mediaURL.size() - 1);
    }

    public void onSaveRoute(View view) {
        Log.d("ROUTE", "Title: " + etTitle.getText() + " Description: " + etDescription.getText());


        if(etTitle.getText().toString().isEmpty()){
            Toast.makeText(EditRouteDetailsActivity.this, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(etDescription.getText().toString().isEmpty()){
            Toast.makeText(EditRouteDetailsActivity.this, "Enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!checkLabeled()) {
            Toast.makeText(EditRouteDetailsActivity.this, "Select at least one label", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            for (LabelCheckBox label : labels) {
                if (label.getCheckBox().isChecked()) {
                    Log.d("LABELS", "Saving route with label: " + label.getLabel().getText());
                }
            }
        }

        progressDialog = new ProgressDialog(EditRouteDetailsActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.progress_dialog_text));
        progressDialog.show();

        Log.d("PHOTO", "Selected image at index: " + horizontalAdapter.getSelectedPos());

        // TODO: Save route object and redirect to detail view of route
        mRoute = new Route(lwaypointArray.size(), bounds,
                etTitle.getText().toString(),
                etDescription.getText().toString(),
                encodedPolyline, aspectRatio,
                mediaURL.get(horizontalAdapter.getSelectedPos()));
        mRoute.setMidPoint(Bounds.getMidPoint(bounds));
        mRoute.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //On Callback get Route Object
                    //Send arraylist to the waypoint model with the route object as a parameter
//                    Toast.makeText(EditRouteDetailsActivity.this, "Waypoints Saved!", Toast.LENGTH_SHORT).show();
                    saveLabelsAndWaypoints(mRoute);

                    // TODO: Redirect to RouteDetailActivity
                }
                else {
                    e.printStackTrace();
                }

            }
        });
    }

    private void callForPhotos() {
        // Make query for all pictures around waypoints
        for (LWaypoint waypoint : lwaypointArray) {
            LatLng latLng = new LatLng(waypoint.getPosition().latitude, waypoint.getPosition().longitude);
            client.getPanoramas(latLng, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONArray photos = response.getJSONArray("photos");
                        for (int i = 0; i < photos.length(); i++) {
                            JSONObject photo = photos.getJSONObject(i);
                            if (!mediaURL.contains(photo.getString("photo_file_url"))){
                                mediaURL.add(photo.getString("photo_file_url"));
                                horizontalAdapter.notifyItemInserted(mediaURL.size() - 1);}
                        }
                        Log.d("PHOTOS", photos.toString());
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("PHOTOS", errorResponse.toString());
                }
            });
        }
    }

    public class HorizontalPhotoAdapter extends RecyclerView.Adapter<HorizontalPhotoAdapter.MyViewHolder> {

        private List<String> horizontalList;

        // Index of currently selected image
        // Defaults to first image
        private int selectedPos = 0;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView ivFriend;

            public MyViewHolder(View view) {
                super(view);
                ivFriend = (ImageView) view.findViewById(R.id.ivFriend);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

                // Get ImageView of previously selected photo if currently loaded in RecyclerView
                // Reset to no padding
                MyViewHolder oldViewHolder = (MyViewHolder) rvPhotos.findViewHolderForAdapterPosition(selectedPos);
                if (oldViewHolder != null) {
                    oldViewHolder.ivFriend.setPadding(0, 0, 0, 0);
                }

                // Set clicked image index to selectedPos and add padding to indicate selected
                Log.d("PHOTO", "new selected position: " + getAdapterPosition());
                selectedPos = getAdapterPosition();

                ivFriend.setPadding(pad, pad, pad, pad);
            }
        }

        public HorizontalPhotoAdapter(List<String> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_photo_view, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            // Clear image
            holder.ivFriend.setImageResource(0);

            // Load image from url
            Picasso.with(getApplicationContext()).load(horizontalList.get(position)).into(holder.ivFriend);

            // Set default no padding
            holder.ivFriend.setPadding(0, 0, 0, 0);

            // Add padding for selected image, defaults to first image
            if(position == selectedPos) {
                Log.d("PHOTO", "holder.isSelected is true for position: " + position);
                holder.ivFriend.setPadding(pad, pad, pad, pad);
            }
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }

        // Returns index of selected image
        public int getSelectedPos() {
            return selectedPos;
        }

    }
}

package com.facebook.seagull.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.seagull.R;
import com.facebook.seagull.adapters.RecyclerViewDataAdapter;
import com.facebook.seagull.adapters.RoutesArrayAdapter;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.SectionRouteModel;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, RoutesArrayAdapter.onUserClickListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;
    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    ArrayList<String> friendsPicUrl = new ArrayList<String>();
    ArrayList<String> friendsID = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();

    ArrayList<Route> myRoutes = new ArrayList<>();
    ArrayList<Route> likedRoutes = new ArrayList<>();

    // for multi-hor view in one vert Recyclerview
    ArrayList<SectionRouteModel> allRouteSections = new ArrayList<>();

    private HorizontalAdapter horizontalAdapter;
    private RecyclerViewDataAdapter adapter;

    private String fbid; // for viweing other user profile

    @BindView(R.id.llMainTitleContainer) LinearLayout llMainTitleContainer;
    @BindView(R.id.tvMainTitle) TextView tvMainTitle;
    @BindView(R.id.appBarLayout) AppBarLayout appBarLayout;
    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.ivProfPic) ImageView ivProfPic;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.rvHorizontal) RecyclerView rvHorizontal;
    @BindView(R.id.rvFull) RecyclerView rvFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        appBarLayout.addOnOffsetChangedListener(this);
        mToolbar.inflateMenu(R.menu.profile_menu);
        startAlphaAnimation(tvMainTitle, 0, View.INVISIBLE);
        ivProfPic.setImageResource(0);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        rvFull.setHasFixedSize(true);
        adapter = new RecyclerViewDataAdapter(this, allRouteSections);
        rvFull.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvFull.setAdapter(adapter);

        horizontalAdapter = new HorizontalAdapter(friendsPicUrl);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(ProfileActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvHorizontal.setLayoutManager(horizontalLayoutManager);
        rvHorizontal.setAdapter(horizontalAdapter);

        rvHorizontal.setNestedScrollingEnabled(false);

        Bundle parameters = new Bundle();
        parameters.putString("fields", "installed");

        if (getIntent().getStringExtra("id") == null) {
            // personal profile
            try {
                String name = ParseUser.getCurrentUser().fetchIfNeeded().getString("first_name") + " " + ParseUser.getCurrentUser().fetchIfNeeded().getString("last_name");
                tvName.setText(name);
                tvMainTitle.setText(ParseUser.getCurrentUser().fetchIfNeeded().getString("first_name"));
                Glide.with(getApplicationContext()).load(ParseUser.getCurrentUser().fetchIfNeeded().getString("picture")).into(ivProfPic);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            final GraphRequest request = GraphRequest.newMyFriendsRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray array, GraphResponse response) {
                            // get the user fbid who also installed app
                            JSONObject result = response.getJSONObject();
                            try {
                                JSONArray data = result.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++ ) {
                                    // Define the class we would like to query
                                    ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
                                    query.whereEqualTo("fbid", data.getJSONObject(i).getString("id") );
                                    query.findInBackground(new FindCallback<ParseUser>() {
                                        public void done(List<ParseUser> itemList, ParseException e) {
                                            if (e == null) {
                                                if (!itemList.isEmpty()) {

                                                    String picUrl = itemList.get(0).getString("picture");
                                                    friendsPicUrl.add(picUrl);
                                                    friendsID.add(itemList.get(0).getObjectId());
                                                    names.add(itemList.get(0).getString("first_name") );
                                                    horizontalAdapter.notifyItemInserted(friendsPicUrl.size() - 1);
                                                }
                                            } else {
                                                Log.d("item", "Error: " + e.getMessage());
                                            }
                                        }
                                    });
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            request.setParameters(parameters);
            request.executeAsync();

            queryForUserRoutes();
            queryForLikedRoutes();

        } else {
            // Other user profile
            CardView friends = (CardView) findViewById(R.id.friendsCard);
            ((ViewGroup) friends.getParent()).removeView(friends);

            String id = getIntent().getStringExtra("id");
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            query.getInBackground(id, new GetCallback<ParseUser>() {
                public void done(ParseUser item, ParseException e) {
                    if (e == null) {
                        String firstName = item.getString("first_name");
                        String lastName = item.getString("last_name");
                        fbid = item.getString("fbid");
                        tvName.setText(firstName + " " + lastName);
                        tvMainTitle.setText(firstName);
                        Glide.with(getApplicationContext()).load(item.getString("picture")).into(ivProfPic);

                        ParseQuery<Route> createdRoutes = ParseQuery.getQuery("Route");
                        createdRoutes.whereEqualTo("user", item);
                        createdRoutes.include("user");
                        createdRoutes.findInBackground(new FindCallback<Route>() {
                            @Override
                            public void done(List<Route> objects, ParseException e) {
                                if (e == null) {
                                    for(Route route : objects) {
                                        Log.d("ROUTE_ACTIVITY", "User created route: " + route.getName());
                                        myRoutes.add(route);
                                    }

                                    if (myRoutes.size() != 0) {
                                        final SectionRouteModel created = new SectionRouteModel("Created", myRoutes);
                                        allRouteSections.add(created);
                                        adapter.notifyDataSetChanged();
                                    }
                                    Log.d("ROUTE_ACTIVITY", "Routes created: " + objects.size());
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });

                        ParseQuery<Route> query = ParseQuery.getQuery("Route");
                        query.whereEqualTo("like", item);
                        query.include("user");
                        query.findInBackground(new FindCallback<Route>() {
                            @Override
                            public void done(List<Route> objects, ParseException e) {
                                if (e == null) {
                                    for(Route route : objects) {
                                        Log.d("ROUTE_ACTIVITY", "User like route: " + route.getName());
                                        likedRoutes.add(route);
                                    }

                                    if (likedRoutes.size() != 0) {
                                        final SectionRouteModel liked = new SectionRouteModel("Liked", likedRoutes);
                                        allRouteSections.add(liked);
                                        adapter.notifyDataSetChanged();
                                    }

                                    Log.d("ROUTE_ACTIVITY", "Routes liked: " + objects.size());
                                } else {
                                    Log.d("ROUTE_LIKE", e.toString());
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        // something went wrong
                    }
                }
            });

        }

    }

    // Get all Current User's Liked Routes
    private void queryForLikedRoutes() {
        ParseQuery<Route> query = ParseQuery.getQuery("Route");
        query.whereEqualTo("like", ParseUser.getCurrentUser());
        query.include("user");
        query.findInBackground(new FindCallback<Route>() {
            @Override
            public void done(List<Route> objects, ParseException e) {
                if (e == null) {
                    for(Route route : objects) {
                        Log.d("ROUTE_ACTIVITY", "User like route: " + route.getName());
                        likedRoutes.add(route);
                    }

                    final SectionRouteModel liked = new SectionRouteModel("Liked", likedRoutes);
                    allRouteSections.add(liked);

                    adapter.notifyDataSetChanged();
//                    adapter.notifyItemInserted(allRouteSections.size() - 1);

                    Log.d("ROUTE_ACTIVITY", "Routes liked: " + objects.size());
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void queryForUserRoutes() {
        ParseQuery<Route> createdRoutes = ParseQuery.getQuery("Route");
        createdRoutes.whereEqualTo("user", ParseUser.getCurrentUser());
        createdRoutes.include("user");
        createdRoutes.findInBackground(new FindCallback<Route>() {
            @Override
            public void done(List<Route> objects, ParseException e) {
                if (e == null) {
                    for(Route route : objects) {
                        Log.d("ROUTE_ACTIVITY", "User like route: " + route.getName());
                        myRoutes.add(route);
                    }

                    final SectionRouteModel created = new SectionRouteModel("Created", myRoutes);
                    allRouteSections.add(created);
//                    adapter.notifyItemInserted(allRouteSections.size() - 1);

                    adapter.notifyDataSetChanged();

                    Log.d("ROUTE_ACTIVITY", "Routes created: " + objects.size());
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onUserClick(String userId) {
        if (ParseUser.getCurrentUser().getObjectId().equals(userId)) {
            // other use so pass in ID (parse)
            Intent i = new Intent(this, HomeActivity.class);
            i.putExtra("profile", "personal");
            startActivity(i);

        } else {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("id", userId);
            startActivity(i);
        }

    }

    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

        private List<String> horizontalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivFriendPic;
            public TextView tvName;

            public MyViewHolder(View view) {
                super(view);
                ivFriendPic = (ImageView) view.findViewById(R.id.ivFriendPic);
                tvName = (TextView) view.findViewById(R.id.tvName);
            }
        }

        public HorizontalAdapter(List<String> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            Picasso.with(getApplicationContext()).load(horizontalList.get(position)).into(holder.ivFriendPic);
            if (names.size() > position) {
                holder.tvName.setText(names.get(position));
            } else {}
            holder.ivFriendPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ProfileActivity.this, ProfileActivity.class);
                    i.putExtra("id", friendsID.get(position) );
                    startActivity(i);

                }
            });
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Cool animation stuff
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if(!mIsTheTitleVisible) {
                startAlphaAnimation(tvMainTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(tvMainTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(llMainTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }
        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(llMainTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

}




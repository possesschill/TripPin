package com.facebook.seagull.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.seagull.R;
import com.facebook.seagull.activities.ProfileActivity;
import com.facebook.seagull.adapters.RecyclerViewDataAdapter;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.SectionRouteModel;
import com.parse.FindCallback;
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

public class UserProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;
    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;
    private boolean otherListLoaded = false;

    private boolean friendsLoaded;

    private ArrayList<String> friendsPicUrl;
    private ArrayList<String> friendsID;
    private ArrayList<String> names;
    private ArrayList<Route> myRoutes;
    private ArrayList<Route> likedRoutes;

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
    @BindView(R.id.rlLoading) RelativeLayout rlLoading;
    @BindView(R.id.llRoutes) LinearLayout llRoutes;

    @BindView(R.id.rlFriendsLoading) RelativeLayout rlFriendsLoading;
    @BindView(R.id.llFriends) LinearLayout llFriends;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance(int page) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsPicUrl = new ArrayList<>();
        friendsID = new ArrayList<>();
        names = new ArrayList<>();
        myRoutes = new ArrayList<>();
        likedRoutes = new ArrayList<>();

        adapter = new RecyclerViewDataAdapter(getContext(), allRouteSections);
        horizontalAdapter = new HorizontalAdapter(friendsPicUrl);

        populateFeed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.bind(this, view);

        if (!friendsLoaded) {
            llFriends.setVisibility(View.GONE);
            rlFriendsLoading.setVisibility(ProgressBar.VISIBLE);
        } else {
            llFriends.setVisibility(View.VISIBLE);
            rlFriendsLoading.setVisibility(ProgressBar.INVISIBLE);
        }

        appBarLayout.addOnOffsetChangedListener(this);
        mToolbar.inflateMenu(R.menu.profile_menu);
        startAlphaAnimation(tvMainTitle, 0, View.INVISIBLE);

        rvFull.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvFull.setAdapter(adapter);

        rvHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHorizontal.setAdapter(horizontalAdapter);
        rvHorizontal.setNestedScrollingEnabled(false);

        mPage = getArguments().getInt(ARG_PAGE);

        allRouteSections.clear();
        likedRoutes.clear();
        myRoutes.clear();
        adapter.notifyDataSetChanged();
        queryForUserRoutes();
        queryForLikedRoutes();

        llRoutes.setVisibility(View.GONE);
        rlLoading.setVisibility(ProgressBar.VISIBLE);

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            String name = ParseUser.getCurrentUser().fetchIfNeeded().getString("first_name") + " " + ParseUser.getCurrentUser().fetchIfNeeded().getString("last_name");
            tvName.setText(name);
            tvMainTitle.setText(ParseUser.getCurrentUser().fetchIfNeeded().getString("first_name"));
            Glide.with(getContext()).load(ParseUser.getCurrentUser().fetchIfNeeded().getString("picture")).into(ivProfPic);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void populateFeed() {
        // personal profile

        Bundle parameters = new Bundle();
        parameters.putString("fields", "installed");

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

                            rlFriendsLoading.setVisibility(ProgressBar.INVISIBLE);
                            llFriends.setVisibility(View.VISIBLE);
                            friendsLoaded = true;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        request.setParameters(parameters);
        request.executeAsync();
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

                    if (likedRoutes.size() != 0) {
                        final SectionRouteModel liked = new SectionRouteModel("Liked", likedRoutes);
                        allRouteSections.add(liked);
                        adapter.notifyDataSetChanged();
                    }
                    // Other query completed, so stop progress bar
                    if(otherListLoaded) {
                        rlLoading.setVisibility(ProgressBar.INVISIBLE);
                        llRoutes.setVisibility(View.VISIBLE);
                    }
                    // Reset boolean for next load
                    otherListLoaded = !otherListLoaded;

                    Log.d("ROUTE_ACTIVITY", "Routes liked: " + objects.size());
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void queryForUserRoutes() {
        ParseQuery<Route> createdRoutes = ParseQuery.getQuery("Route");
        createdRoutes.whereEqualTo("user", ParseUser.getCurrentUser() );
        createdRoutes.include("user");
        createdRoutes.findInBackground(new FindCallback<Route>() {
            @Override
            public void done(List<Route> objects, ParseException e) {
                if (e == null) {
                    for(Route route : objects) {
                        Log.d("ROUTE_ACTIVITY", "User like route: " + route.getName());
                        myRoutes.add(route);
                    }

                    if (myRoutes.size() != 0) {
                        final SectionRouteModel created = new SectionRouteModel("Created", myRoutes);
                        allRouteSections.add(created);
                        adapter.notifyDataSetChanged();
                    }
                    // Other query completed, so stop progress bar
                    if(otherListLoaded) {
                        rlLoading.setVisibility(ProgressBar.INVISIBLE);
                        llRoutes.setVisibility(View.VISIBLE);
                    }
                    // Reset boolean for next load
                    otherListLoaded = !otherListLoaded;

                    Log.d("ROUTE_ACTIVITY", "Routes created: " + objects.size());
                } else {
                    e.printStackTrace();
                }
            }
        });

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

            Picasso.with(getContext()).load(horizontalList.get(position)).into(holder.ivFriendPic);
            if (names.size() > position) {
                holder.tvName.setText(names.get(position));
            } else {}
            holder.ivFriendPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), ProfileActivity.class);
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

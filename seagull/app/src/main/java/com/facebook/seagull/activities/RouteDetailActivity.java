package com.facebook.seagull.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.seagull.R;
import com.facebook.seagull.adapters.RouteDetailFragmentPagerAdapter;
import com.facebook.seagull.adapters.SmartFragmentStatePagerAdapter;
import com.facebook.seagull.listeners.LikeListener;
import com.facebook.seagull.models.Route;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteDetailActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.sliding_tabs) TabLayout tabLayout;
    @BindView(R.id.ivMain) ImageView ivMain;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.ivUser) ImageView ivUser;
    @BindView(R.id.tvUserName) TextView tvUserName;
    @BindView(R.id.btnLike) ImageView btnLike;
    @BindView(R.id.tvNumLikes) TextView tvNumLikes;

    private String routeId;
    private int numLikes;
    private String routeName;
    private ParseUser creator;
    private ArrayList<ParseUser> usersWhoLiked = new ArrayList<>();

    private SmartFragmentStatePagerAdapter adapterViewPager;
    private static String tabTitles[] = new String[] { "Map", "Media", "Details"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        ButterKnife.bind(this);
        this.routeId = getIntent().getStringExtra("route_id");
        
        setupWindowAnimations();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        adapterViewPager = new RouteDetailFragmentPagerAdapter(getSupportFragmentManager(), RouteDetailActivity.this, this.routeId);
        viewPager.setAdapter(adapterViewPager);

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        // Attach the page change listener inside the activity
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position);
                // Wanna change title based on page? ->
                // getSupportActionBar().setTitle(tabTitles[position]);
            }
            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

        viewPager.setCurrentItem(0);
        doParseQueries();

        getSupportActionBar().setTitle("Details");
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(5000);
        getWindow().setEnterTransition(slide);
//        Fade fade = new Fade();
//        fade.setDuration(1000);
//        getWindow().setEnterTransition(fade);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.route_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.create_party) {
            Intent i = new Intent(this, PartyCreateActivity.class);
            i.putExtra("route_id", routeId);
            startActivity(i);
            return true;
        } else if (id == R.id.menu_location) {
            Intent i = new Intent(this, RouteDetailMapActivity.class);
            i.putExtra("route_id", this.routeId);
            startActivity(i);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doParseQueries() {
        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
        query.include("user");
        query.getInBackground(this.routeId, new GetCallback<Route>() {
            public void done(Route item, ParseException e) {
                if (e == null) {

                    // LOAD ROUTE INFO
                    routeName = item.getName();

                    tvName.setText(routeName);
                    Glide.with(getApplicationContext()).load(item.getPhotoUrl()).into(ivMain);

                    // LOAD CREATOR INFO
                    creator = item.getUser();
                    tvUserName.setText(creator.get("first_name") + " " + creator.get("last_name"));

                    // Load profile picture of creator as circle
                    Glide.with(getApplicationContext()).load(creator.get("picture")).asBitmap().centerCrop().into(new BitmapImageViewTarget(ivUser) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            ivUser.setImageDrawable(circularBitmapDrawable);
                        }
                    });

                    // LOAD USER LIKE INFO

                    // Default gray heart for unliked
                    btnLike.setTag(false);
                    btnLike.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.heartgray, null));

                    // Query Parse database to check if route was liked
                    // Sets drawable accordingly
                    LikeListener.queryLiked(getApplicationContext(), item, btnLike);

                    // Sets listener to like and unlike route and update database
                    LikeListener likes = new LikeListener(getApplicationContext(), btnLike, item, tvNumLikes);
                    btnLike.setOnClickListener(likes);

                    likes.updateLikes();
                }
                else {
                    Log.d("RouteDetailMapFragment", e.toString());
                }
            }
        });
    }
}

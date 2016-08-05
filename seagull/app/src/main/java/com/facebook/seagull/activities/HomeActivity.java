package com.facebook.seagull.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.View;

import com.facebook.seagull.R;
import com.facebook.seagull.adapters.RoutesArrayAdapter;
import com.facebook.seagull.fragments.HomeRouteFeedFragment;
import com.facebook.seagull.fragments.HomeRouteMapFragment;
import com.facebook.seagull.fragments.PartyFeedFragment;
import com.facebook.seagull.fragments.UserProfileFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.parse.ParseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeActivity extends AppCompatActivity implements RoutesArrayAdapter.onUserClickListener {
    protected static final int ACCESS_FINE_LOCATION_REQUEST = 70;
    BottomBar mBottomBar;

    List<Fragment> fragments = new ArrayList<>(5);
    private FragNavController fragNavController;
    @BindView(R.id.fabCreateMap)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        HomeRouteMapFragment homeRouteMapFragment = HomeRouteMapFragment.newInstance(0);

        // Add fragments to bottom bar
        fragments.add(homeRouteMapFragment);
        fragments.add(HomeRouteFeedFragment.newInstance(1));
        fragments.add(PartyFeedFragment.newInstance(2));
        fragments.add(UserProfileFragment.newInstance(3));

        fragNavController = new FragNavController(getSupportFragmentManager(), R.id.myScrollingContent, fragments);

        mBottomBar = BottomBar.attach(this, savedInstanceState);

        // Gets rid of weird space above toolbar
        mBottomBar.noNavBarGoodness();

        mBottomBar.setTextAppearance(R.style.BottomBarText);
        mBottomBar.setItems(R.menu.four_buttons_menu);

        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {

            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.location_item:
                        fragNavController.switchTab(FragNavController.TAB1);
                        fab.show();
                        break;
                    case R.id.list_item:
                        fragNavController.switchTab(FragNavController.TAB2);
                        fab.show();
                        break;
                    case R.id.party_item:
                        fragNavController.switchTab(FragNavController.TAB3);
                        fab.hide();
                        break;
                    case R.id.profile_item:
                        fragNavController.switchTab(FragNavController.TAB4);
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                fragNavController.clearStack();
            }
        });

        // Setting colors for different tabs when there's more than three of them.
        // You can set colors for tabs in three different ways as shown below.
        mBottomBar.mapColorForTab(0, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mBottomBar.mapColorForTab(1, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mBottomBar.mapColorForTab(2, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mBottomBar.mapColorForTab(3, ContextCompat.getColor(this, R.color.colorPrimaryDark));

        if (getIntent().getStringExtra("profile") != null) {
            mBottomBar.selectTabAtPosition(FragNavController.TAB4, false);
        }

        setupWindowAnimations();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }

    public void onStartMapCreation(View view) {
        Intent i = new Intent(HomeActivity.this, MapActivity.class);
        startActivity(i);
    }
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void onUserClick(String userId) {
        if (ParseUser.getCurrentUser().getObjectId().equals(userId)) {
            // other use so pass in ID (parse)
//            fragNavController.switchTab(FragNavController.TAB4);
            mBottomBar.selectTabAtPosition(FragNavController.TAB4, false);
        } else {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("id", userId);
            startActivity(i);
        }
    }

}

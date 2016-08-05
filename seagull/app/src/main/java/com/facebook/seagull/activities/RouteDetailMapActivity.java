package com.facebook.seagull.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.facebook.seagull.R;
import com.facebook.seagull.fragments.RouteDetailMapFragment;

public class RouteDetailMapActivity extends AppCompatActivity {

    private String routeId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail_map);

        routeId = getIntent().getStringExtra("route_id");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.your_placeholder,  RouteDetailMapFragment.newInstance(0, this.routeId));
        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Complete the changes added above
        ft.commit();

    }
}

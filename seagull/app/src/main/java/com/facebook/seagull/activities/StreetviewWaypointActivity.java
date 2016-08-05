package com.facebook.seagull.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetviewWaypointActivity extends AppCompatActivity {

    // George St, Sydney
    private LatLng loc;
    private Handler mHandler = new Handler();
    private static final int QUERY_DELAY_MS = 1000;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_streetview_waypoint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Make sure the toolbar exists and add back button

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Street View");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Double latitude = getIntent().getDoubleExtra("latitude", -33.8736);
        Double longitude = getIntent().getDoubleExtra("longitude", 151.20689);

        loc = new LatLng(latitude, longitude);
        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(final StreetViewPanorama panorama) {
                // Only set the panorama to SYDNEY on startup (when no panoramas have been
                // loaded which is when the savedInstanceState is null).
                if (savedInstanceState == null) {
                    panorama.setPosition(loc);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (panorama.getLocation() != null) {
                                // your actions here
                            } else {
                                finish();
                                Toast.makeText(getApplicationContext(), "Sorry, StreetView is unavailable for this location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, QUERY_DELAY_MS);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}


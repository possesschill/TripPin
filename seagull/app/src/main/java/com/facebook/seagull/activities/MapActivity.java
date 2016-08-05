package com.facebook.seagull.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.clients.GoogleClient;
import com.facebook.seagull.models.LWaypoint;
import com.facebook.seagull.models.MarkerPoint;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.parsers.Bounds;
import com.facebook.seagull.parsers.MPolyline;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;



@RuntimePermissions
public class MapActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener {
    protected static final int ACCESS_FINE_LOCATION_REQUEST = 70;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private final int REQUEST_CODE = 20;
    private final int PLACE_PICKER_REQUEST = 4;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ArrayList<LWaypoint> lwaypointArray;
    private ArrayList<MarkerPoint> markerPointArrayList;
    private Bounds bounds;
    private String encodedPolyline;
    private Route mRoute;
    private double count;
    private GoogleClient client;
    FloatingActionButton fab;
    FloatingActionButton fabSearch;
    private Toolbar toolbar;
    private boolean pointSelectionAllowed = true;
    private MapActivity context;


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 60000; // changed to 60 secs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fab =  (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        context = this;
        //initializing
        lwaypointArray = new ArrayList<>();
        markerPointArrayList = new ArrayList<>();
        count = 0;
        client = new GoogleClient();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //deal with exceptions by checking if map fragment is null and then handling appropriately
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    loadMap(googleMap);
                }
            });
        } else {
        }

        // AUTO COMPLETE MODAL OVERLAY FOR SEARCHING PLACES
        // WHEN BUTTON CLICKED, OVERLAY POPS UP, YOU SEARCH FOR PLACE AND ON SELECTED PLACE,
        // PLACE A MARKER
        // WILL GO THROUGH ON MONDAY
        //setupWindowAnimations();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create a Trip");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("MAP", "Place: " + place.getName());
                // Creates and adds marker to the map

                showAlertDialogForPoint(place.getLatLng());

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13));

                // We need to add this point to the arraylist
                //move map to this waypoint
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("MAP", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13));
                showAlertDialogForPoint(place.getLatLng());
            }
        }
    }
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            MapActivityPermissionsDispatcher.getMyLocationWithCheck(this);
            map.setOnMapClickListener(this);
            map.setOnMapLongClickListener(this);
            //overrides marker click
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    mapMarkerClicked(marker);

                    return false;
                }
            });
            //map.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        } else {
        }
    }

    private void mapMarkerClicked(Marker marker) { // called when marker is clicked
        MarkerPoint markerPoint = findMarker(marker);//finds marker
        if (markerPoint!= null){
            createConfirmDialog(markerPointArrayList.indexOf(markerPoint));//pops up
        }
    }
    private MarkerPoint findMarker(Marker marker) {
        for(MarkerPoint markerPoint : markerPointArrayList){
            if(markerPoint.getMarker().getId().equals(marker.getId())){
                return markerPoint;
            }
        }
        MarkerPoint safety = null;
        return safety;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            MapActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            MapActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        }
    }

    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            // Now that map has loaded, let's get our location!
            map.setMyLocationEnabled(true);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            connectClient();
        }
    }
    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    public void onSearch(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MapActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(pointSelectionAllowed) {
            pointSelectionAllowed = false;
            showAlertDialogForPoint(latLng);
        }
        else{
            Toast.makeText(MapActivity.this,"Fetching waypoint data", Toast.LENGTH_SHORT).show();
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    @Override
    public void onMapLongClick(LatLng latLng) {//starts adding waypoint story
        if(pointSelectionAllowed) {
            pointSelectionAllowed = false;
            showAlertDialogForPoint(latLng);
        }
        else{
            Toast.makeText(MapActivity.this,"Fetching waypoint data", Toast.LENGTH_SHORT).show();
        }
        showAlertDialogForPoint(latLng);
    }
    // Display the alert that adds the marker
    private void showAlertDialogForPoint(final LatLng point) {
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker();
        client.getPlaceInfo(this, point, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Geocoding Result", response.toString());
                //get feedback
                //populate waypoint
                LWaypoint templWayPoint =  new LWaypoint();

                String name = "-1";
                try {

                    //name = (String) response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(0).get("long_name");
                    name = (String) response.getJSONArray("results").getJSONObject(0).get("formatted_address");
                    Log.d("long_name", name);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Marker marker= map.addMarker(new MarkerOptions()
                        .position(point)
                        //.draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)));//.icon(customMarker));
                if(!name.equals("-1")) {
                    templWayPoint.setName(name);
                }
                else{
                    templWayPoint.setName("");
                }
                markerPointArrayList.add(new MarkerPoint(marker,templWayPoint));
                dropPinEffect(marker);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                throwable.printStackTrace();
                String name = " ";
                LWaypoint templWayPoint =  new LWaypoint();
                templWayPoint.setName(name);
                Marker marker= map.addMarker(new MarkerOptions()
                        .position(point)
                        //.draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)));
                markerPointArrayList.add(new MarkerPoint(marker,templWayPoint));
                dropPinEffect(marker);
            }


        });


    }
    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1000;
        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 8 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                   // marker.showInfoWindow();
                    createConfirmDialog(markerPointArrayList.size()-1);

                }
            }
        });
    }
    private void createConfirmDialog(final int i) {
        //creates dialog
        final Dialog myDialog = new Dialog(MapActivity.this, R.style.CustomDialogTheme);
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setContentView(R.layout.waypoint_custom_dialog);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final EditText etTitle = (EditText) myDialog.findViewById(R.id.etTitle) ;
        final EditText etDescription = (EditText) myDialog.findViewById(R.id.etDescription);

        myDialog.show();
        //updates fields to current values for corresponding waypoint
        etTitle.setText(markerPointArrayList.get(i).getlWaypoint().getName());
        etDescription.setText(markerPointArrayList.get(i).getlWaypoint().getDescription());
        Button confirm_btn = (Button) myDialog.findViewById(R.id.btnConfirm);
        Button delete_btn = (Button) myDialog.findViewById(R.id.btnDelete);
        delete_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                deleteMarker(markerPointArrayList.get(i));
                myDialog.dismiss();
                pointSelectionAllowed = true;//allows another waypoint to be selected, done here so gap between dialog loading isnt accessible
                checkFabState();
            }
        });
        //controls what happens when confirm button is pressed
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Marker marker = markerPointArrayList.get(i).getMarker();
                markerPointArrayList.get(i).lWaypoint.setPosition(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude));
                markerPointArrayList.get(i).lWaypoint.setOrder(count);
                markerPointArrayList.get(i).lWaypoint.setName(etTitle.getText().toString());
                markerPointArrayList.get(i).lWaypoint.setDescription(etDescription.getText().toString());

                count+=1;
                myDialog.dismiss();
                pointSelectionAllowed = true;//allows another waypoint to be selected, done here so gap between dialog loading isnt accessible
                checkFabState();
            }
        });
    }

    private void checkFabState() {
        Animation showFab = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_show);
        Animation hideFab = AnimationUtils.loadAnimation(getApplication(),R.anim.fab_hide);
        if(markerPointArrayList.size()<2){
            fab.startAnimation(hideFab);
            fab.hide();
            fab.setClickable(false);
        }
        else{
            if(fab.getVisibility()!=View.VISIBLE) {//checks if fab is visible and needsto run animation
                fab.startAnimation(showFab);
                fab.show();
                fab.setClickable(true);
            }
        }
    }
    private void deleteMarker(MarkerPoint markerPoint) {
        Marker marker = markerPoint.getMarker();
        marker.remove();
        markerPointArrayList.remove(markerPoint);
    }
    @Override
    public void onConnected(@Nullable Bundle dataBundle) {
        // Display the connection status
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST );
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            map.moveCamera(cameraUpdate);
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST );
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    public void onSaveRoute(View v){

        lwaypointArray.clear();
        for(MarkerPoint markerPoint : markerPointArrayList){
            lwaypointArray.add(markerPoint.getlWaypoint());
        }
        //Call directions api
        RequestParams params = new RequestParams();
        StringBuilder origin = new StringBuilder();
        origin.append(Double.toString(lwaypointArray.get(0).getPosition().latitude));
        origin.append(", ");
        origin.append(Double.toString(lwaypointArray.get(0).getPosition().longitude));
        origin.toString();

        StringBuilder destination = new StringBuilder();
        destination.append(Double.toString(lwaypointArray.get(lwaypointArray.size()-1).getPosition().latitude));
        destination.append(", ");
        destination.append(Double.toString(lwaypointArray.get(lwaypointArray.size()-1).getPosition().longitude));
        destination.toString();

        params.put("origin", origin);
        params.put("destination", destination);

        if(lwaypointArray.size()>2) {
            StringBuilder waypoint = new StringBuilder();

            for (int x = 1; x < lwaypointArray.size() - 1; x++) {
                waypoint.append(Double.toString(lwaypointArray.get(x).getPosition().latitude));
                waypoint.append(", ");
                waypoint.append(Double.toString(lwaypointArray.get(x).getPosition().longitude));
                if (x != lwaypointArray.size() - 2) {
                    waypoint.append("|");
                }
            }
            params.put("waypoints", waypoint);
        }
        params.put("key", getResources().getString(R.string.google_maps_key));

        client.getDirections(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("ROUTE_ACTIVITY", "onSuccess");

                try {
                    JSONArray routes = response.getJSONArray("routes");

                    if (routes.length() < 1) {
                        Toast.makeText(MapActivity.this, "No route exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject primaryRoute = routes.getJSONObject(0);

                    // Get overview_polyline
                    JSONObject overview_polyline = primaryRoute.getJSONObject("overview_polyline");
                    encodedPolyline = overview_polyline.getString("points");
                    List<LatLng> list = MPolyline.decodePoly(encodedPolyline);
                    MPolyline.drawPolyLineOnMap(list, map, MapActivity.this);
                    Log.d("ROUTE_ACTIVITY", "overview_polyline: " + encodedPolyline);
                    LatLngBounds latLngBounds = buildBounds(list);
                    // Get bounds
                    bounds = new Bounds(primaryRoute.getJSONObject("bounds"));
                    Intent i = new Intent(MapActivity.this, EditRouteDetailsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("encodedPolyline", encodedPolyline);
                    i.putExtra("bounds", Parcels.wrap(bounds));
                    i.putExtra("waypointArray",lwaypointArray );
                    i.putExtra("aspectRatio", calculateAspectRatio(latLngBounds));
                    startActivityForResult(i, REQUEST_CODE);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    setResult(RESULT_OK);
                    finish();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    @Override
    public void onBackPressed() {
        finish();
        //Alert dialog warning that going back would lose all changes,
        //if yes to sure they wanna go, restart map activity
        //if no do nothing
    }
    // Create bounds from points in polyline
    private LatLngBounds buildBounds (List<LatLng> list) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latlng : list) {
            builder.include(latlng);
        }
        return builder.build();
    }
    // Calculate aspect ratio using bounds generated from polyline points
    private double calculateAspectRatio(LatLngBounds bounds){
        LatLng northeast = bounds.northeast;
        LatLng southwest = bounds.southwest;

        // find corner relative to northeast and southwest
        LatLng southeastCorner = new LatLng(southwest.latitude, northeast.longitude);

        // find width and height from bounds to corner
        double width = SphericalUtil.computeDistanceBetween(southwest, southeastCorner);
        double height = SphericalUtil.computeDistanceBetween(northeast, southeastCorner);

        // return raw aspect ratio, limit in later usages
        return width/height;
    }

}
package com.facebook.seagull.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.RouteDetailActivity;
import com.facebook.seagull.clients.LabelClient;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.LabelCheckBox;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.facebook.seagull.parsers.MPolyline;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class HomeRouteMapFragment extends Fragment implements com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //Taken from the Activity  androdi java class
    public static final int RESULT_OK = -1;
    public static final int RESULT_CANCELED    = 0;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int ACCESS_FINE_LOCATION_REQUEST = 700;
    public static final String ARG_PAGE = "ARG_PAGE";
    public GoogleMap map;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<Polyline> polyList;
    private boolean flag;
    Handler m_handler;
    Runnable m_runnable;
    CoordinatorLayout cdLayout;
    private Route currRoute;
    private int mPage;
    private Snackbar snackbar;
    private ArrayList<Label> filters;
    private LabelClient labelMaker;
    private ArrayList<LabelCheckBox> labelObjects;
    Toolbar toolbar;

    // Adapter for the Parse query
    private ParseQueryAdapter<Waypoint> waypointsQueryAdapter;
    private ParseQueryAdapter<Route> routesQueryAdapter;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LatLng currentLoc;
    private ArrayList<Route> nearbyRoutes;
    private long UPDATE_INTERVAL = 60000000;  /* 60000 secs */
    private long FASTEST_INTERVAL = 60000; /* 5 secs */ // changed to 60 secs

    // TODO: Show route, add markers in route (but not make them editable)

    public static HomeRouteMapFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        HomeRouteMapFragment fragment = new HomeRouteMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nearbyRoutes = new ArrayList<>();
        polyList = new ArrayList<>();
        filters = new ArrayList<>();
        flag = false;
        snackbar = null;

        labelMaker = new LabelClient();
        labelObjects = labelMaker.querySearchOptions();
        Log.d("ROUTE", "NEW FRAGMENT");


    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_route_detail_map, container, false);
        ButterKnife.bind(this, view);
        this.mPage = getArguments().getInt(ARG_PAGE);
        this.mapView = (MapView) view.findViewById(R.id.mapDetail);
        this.mapView.onCreate(savedInstanceState);
        this.cdLayout = (CoordinatorLayout) view.findViewById(R.id.home_activity);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("TripPin");
        toolbar.inflateMenu(R.menu.home_map_fragment_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {


                    case R.id.action_search:
                        try {
                            Intent intent =
                                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                            .build(getActivity());
                            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                        } catch (GooglePlayServicesRepairableException e) {
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                        }
                        break;
                    case R.id.action_filter:
                        openFilterDialog();
                        break;

                }
                return true;
            }
        });

        return view;
    }

    public void openFilterDialog(){

        // Opens custom dialog to select filters
        Dialog dialog = new Dialog(getContext(), R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_filter);

        // Get layout from dialog to add checkboxes dynamically
//        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.llDialog);
        GridLayout layout = (GridLayout) dialog.findViewById(R.id.glDialog);

        // Add checkboxes for labelObjects to layout
        labelMaker.loadSearchOptions(labelObjects, getContext(), layout);

        // Reloads feed with filters applied and stores filters
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                // Stores filters in labelObjects with current state of checked/unchecked
                labelObjects = labelMaker.storeSearchOptions();

                // Get labels to apply as filters
                ArrayList<Label> onlyLabels = labelMaker.getFilters();

                for (Label aLabel : onlyLabels) {
                    Log.d("LABELS", "Filters checked: " + aLabel.getText());
                }
                reloadWithFilters(onlyLabels);
            }
        });

        dialog.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                loadMap(googleMap);
                googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        onPolylineClickFunction(polyline);
                    }
                });
                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        cameraChanged(cameraPosition);
                    }
                });
            }
        });
    }

    private void cameraChanged(CameraPosition cameraPosition) {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        Log.d("ROUTE_ACTIVITY", "currentloc: " + currentLoc);
        // Get all Waypoints within 20 miles
        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
        ParseGeoPoint sw = new ParseGeoPoint(bounds.southwest.latitude,bounds.southwest.longitude);
        ParseGeoPoint ne = new ParseGeoPoint(bounds.northeast.latitude,bounds.northeast.longitude);
        if (!filters.isEmpty()) {
            query = ParseQuery.or(LabelClient.buildOrQuery(filters));
        }
        query.whereWithinGeoBox("midPoint", sw, ne);
        makeQuery(query);


    }

    private void onPolylineClickFunction(Polyline polyline) {
        Log.d("HomeRoute", "polyline clicked");

        Polyline poly;
        for(int x = 0 ; x< polyList.size(); x++){
            poly = polyList.get(x);
            if(poly.equals(polyline)){// try comparing poly.getPoints with polyline.getPoints
                polyline.setColor(Color.BLUE);


                Route mRoute = nearbyRoutes.get(x);
                currRoute= mRoute;

                LatLng ne = new LatLng(mRoute.getNeLat(),mRoute.getNeLng());
                LatLng sw = new LatLng(mRoute.getSwLat(),mRoute.getSwLng());
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(ne);
                builder.include(sw);
                LatLngBounds cBounds = builder.build();

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(cBounds, 20);
                map.animateCamera(cu);
                String name = mRoute.getName();

                Snackbar locSnackbar = Snackbar
                        .make(mapView, name, Snackbar.LENGTH_INDEFINITE).setAction("Details", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getContext(), RouteDetailActivity.class);
                                i.putExtra("route_id", currRoute.getObjectId().toString());
                                getActivity().startActivity(i);
                            }
                        });
                View sbView = locSnackbar.getView();
                sbView.setBackgroundColor(Color.WHITE);
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

                textView.setTextColor(getResources().getColor(R.color.darkBrown));
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                locSnackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
                locSnackbar.show();
                snackbar = locSnackbar;
            }
            else{//RESETS COLOR OF LAST POLYLINE

                if(poly.getColor()!= ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)){
                    poly.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                }
            }

        }

    }

    // Must override the four methods below or MapView won't work
    // Yes, that's janky
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Log.d("ROUTE", "resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if(snackbar!=null) {
            if (snackbar.isShown()) {
                snackbar.dismiss();
            }
        }

        Log.d("ROUTE", "paused");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Log.d("ROUTE", "destroyed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
        Log.d("ROUTE", "lowmemory");
    }

    protected void loadMap(GoogleMap googleMap) {
        this.map = googleMap;
        if (map != null) {
            // Map is ready
            HomeRouteMapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
            //overrides marker click
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("all")
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            // Now that map has loaded, let's get our location!
            map.setMyLocationEnabled(true);
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getFragmentManager(), "Location Updates");
            }

            return false;
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
    public void onConnected(@Nullable Bundle dataBundle) {
        // Display the connection status
        if(polyList!=null){
            polyList.clear();
            polyList = MPolyline.drawFromRouteArray(nearbyRoutes, map, getContext());
        }
        if (ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST );
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currentLoc = latLng;
            // TODO figure what to do on location updates
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            map.moveCamera(cameraUpdate);
            // new Task1().execute();

//            m_handler=new Handler();
//
//            m_runnable = new Runnable(){
//                public void run() {
//
//                    nearbyQuery();
//                    m_handler.postDelayed(m_runnable, 5000);
//
//                }
//            };
//            m_handler.postDelayed(m_runnable, 0);
        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST );
            return;
        }
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(getContext(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(getContext(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    // TODO if the person is near waypoint, do something
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("ROUTE", msg);
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        nearbyRoutesQuery();//HERE IT IS
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }



    public void nearbyRoutesQuery(){
        Log.d("ROUTE_ACTIVITY", "currentloc: " + currentLoc);
        // Get all Waypoints within 20 miles
        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
        ParseGeoPoint geoPoint = new ParseGeoPoint(currentLoc.latitude, currentLoc.longitude);

        if (!filters.isEmpty()) {
            query = ParseQuery.or(LabelClient.buildOrQuery(filters));
        }
        query.whereWithinMiles("midPoint", geoPoint, 20);
        //query.include("route");
        //query.setLimit(10);
        makeQuery(query);
    }

    private void makeQuery(ParseQuery<Route> query) {

        query.findInBackground(new FindCallback<Route>() {
            @Override
            public void done(List<Route> queryRoutes, ParseException e) {
                if (e != null) {
                    Log.d("ROUTE_ACTIVITY", "Get routes failed");
                    e.printStackTrace();
                }
                else {

                    ArrayList<Route> newRoutes = new ArrayList<Route>();

                    if (queryRoutes.size() == 0) {
                        Log.d("ROUTE_ACTIVITY", "No nearby routes");
                        return;
                    }

                    // Add Routes from nearbyWaypoints to nearbyRoutes ArrayList
                    for (Route route : queryRoutes) {
                        Log.d("ROUTE_ACTIVITY", "Route name: " + route.getName());
                    }

                    Log.d("ROUTE_ACTIVITY", "Number of unique routes: " + queryRoutes.size());

                    if(nearbyRoutes.containsAll(queryRoutes)){//Keeps old routes, does not run code to remove polylines
                        Log.d("ROUTE", "queried routes and routes stored are the same");
                    }
                    else{//adds new routes to routes list, stores old
                        Log.d("ROUTE", "queried is not stored");
                        for(Route route : queryRoutes){
                            if(!nearbyRoutes.contains(route)){
                                newRoutes.add(route);
                                nearbyRoutes.add(route);//stores

                            }
                        }


                    }
                    polyList.addAll(MPolyline.drawFromRouteArray(newRoutes, map, getContext()));//stores new polylines

                    // Process list of nearbyRoutes
                    Log.d("ROUTE", Integer.toString(nearbyRoutes.size()));
                    Log.d("ROUTE", "# OF POLYLINES " + Integer.toString(polyList.size()));

                }
            }
        });
    }


    // Called when filter dialog is closed and new filters exist
    public void reloadWithFilters(ArrayList<Label> mFilters) {
        // Sets filters to list of Labels stored in HomeActivity
        filters = mFilters;
        nearbyRoutes.clear();
        for (Polyline polyline : polyList) {
            polyline.remove();
        }
        polyList.clear();
        nearbyRoutesQuery();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getContext(), data);
                Log.i("MAP", "Place: " + place.getName());
                toolbar.setTitle(place.getName());

                LatLngBounds bounds = place.getViewport();
                if(bounds!=null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                }
                else{
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13));
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getContext(), data);
                // TODO: Handle the error.
                Log.i("MAP", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}

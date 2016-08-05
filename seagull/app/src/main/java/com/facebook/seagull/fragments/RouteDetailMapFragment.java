package com.facebook.seagull.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class RouteDetailMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int ACCESS_FINE_LOCATION_REQUEST = 70;
    public static final String ARG_PAGE = "ARG_PAGE";
    private GoogleMap map;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String routeId;
    private int mPage;
    private ArrayList<ParseGeoPoint> waypointsLocations = new ArrayList<>();
    private LatLng northeast;
    private LatLng southwest;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 60000; /* 5 secs */ // changed to 60 secs

    // TODO: Show route, add markers in route (but not make them editable)

    public RouteDetailMapFragment() {
        // Required empty public constructor
    }

    public static RouteDetailMapFragment newInstance(int page, String routeId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString("route_id", routeId);
        RouteDetailMapFragment fragment = new RouteDetailMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void addMarkers() {
        // Add a marker at each waypoint
        IconGenerator iconFactory = new IconGenerator(getContext());
//        iconFactory.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        iconFactory.setStyle(IconGenerator.STYLE_GREEN);
        for (int i = 0; i < waypointsLocations.size(); i++) {
            ParseGeoPoint loc = waypointsLocations.get(i);
            // i + 1 to start from 1
            addIcon(iconFactory, Integer.toString(i + 1) , new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(northeast);
        builder.include(southwest);
        int boundsPadding = 150;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), boundsPadding);
        map.moveCamera(cameraUpdate);
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        map.addMarker(markerOptions);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set to full screen
        if (getArguments() != null ) {
            this.routeId = getArguments().getString("route_id");
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.getInBackground(this.routeId, new GetCallback<Route>() {
                public void done(Route item, ParseException e) {
                    if (e == null) {
                        // "PROBLEM"
                        ParseQuery<Waypoint> query = ParseQuery.getQuery("Waypoint");
                        query.whereEqualTo("route", item);
                        northeast = new LatLng(item.getNeLat(), item.getNeLng());
                        southwest = new LatLng(item.getSwLat(), item.getSwLng());

                        query.findInBackground(new FindCallback<Waypoint>() {

                            @Override
                            public void done(List<Waypoint> waypointList,
                                             ParseException e) {
                                if (e == null) {
                                    for (Waypoint waypoint : waypointList) {
                                        try {
                                            waypointsLocations.add(waypoint.fetchIfNeeded().getParseGeoPoint("position"));
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    addMarkers();

                                } else {
                                    Log.d("Post retrieval", "Error: " + e.getMessage());
                                }
                            }
                        });
                        // TODO Access route stuff for data population
                        drawPolyline(item);
                    } else {
                        Log.d("RouteDetailMapFragment", e.toString());
                    }
                }
            });
        } else {
            throw new RuntimeException("No route id passed");
        }
    }

    private void drawPolyline(Route route) {

        String LINE = route.getEncodedPolyline();
        List<LatLng> decodedPath = PolyUtil.decode(LINE);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int width = (int) (3 * scale + 0.5f);
        map.addPolyline(new PolylineOptions().width(width).color(ContextCompat.getColor(getContext(), R.color.darkBrown)).addAll(decodedPath));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Removing this avoids crashing but the thing no longer loads the right map
        //        if (container == null) {
        //            return null;
        //        }
        View view = inflater.inflate(R.layout.fragment_route_detail_map, container, false);
        ButterKnife.bind(this, view);
        this.mPage = getArguments().getInt(ARG_PAGE);
        this.mapView = (MapView) view.findViewById(R.id.mapDetail);

        this.mapView.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Route");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                loadMap(googleMap);

            }
        });
    }



    // Must override the four methods below or MapView won't work
    // Yes, that's janky
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    protected void loadMap(GoogleMap googleMap) {
        this.map = googleMap;
        if (map != null) {
            // Map is ready
            RouteDetailMapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
            //overrides marker click
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
//                    mapMarkerClicked(marker);
                    return false;

                }
            });
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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( getActivity());
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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST);
                return;
            }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // TODO figure what to do on location updates
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
//            map.moveCamera(cameraUpdate);
        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST );
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult( getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

}

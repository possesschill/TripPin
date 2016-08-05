package com.facebook.seagull.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.seagull.R;
import com.facebook.seagull.adapters.MediaArrayAdapter;
import com.facebook.seagull.clients.PanoramioClient;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class RouteDetailMediaFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String ROUTE_ID = "route_id";

    // TODO ENSURE ALL ROUTES HAVE A MIDPOINT
    private PanoramioClient client = new PanoramioClient();
    private String routeId;

    private ArrayList<ParseGeoPoint> waypointsLocations = new ArrayList<>();
    private ArrayList<String> mediaURL;
    private MediaArrayAdapter aaMediaURL;

    private int mPage;
    private RecyclerView rvMedia;

    public RouteDetailMediaFragment() {
        // Required empty public constructor
    }

    public static RouteDetailMediaFragment newInstance(int page, String routeId) {
        RouteDetailMediaFragment fragment = new RouteDetailMediaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ROUTE_ID, routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.routeId = getArguments().getString("route_id");
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.getInBackground(this.routeId, new GetCallback<Route>() {
                public void done(Route item, ParseException e) {
                    if (e == null) {

                        ParseQuery<Waypoint> query = ParseQuery.getQuery("Waypoint");
                        query.whereEqualTo("route", item);
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
                                    callForPhotos();
                                } else {
                                    Log.d("Post retrieval", "Error: " + e.getMessage());
                                }
                            }
                        });

                    } else {
                        Log.d("RouteDetailMapFragment", e.toString());
                    }
                }
            });
        }

        mediaURL = new ArrayList<>();
        aaMediaURL = new MediaArrayAdapter(getContext(), mediaURL );

    }

    private void callForPhotos() {
        // TODO Make query for all pictures
        for (ParseGeoPoint waypoint : waypointsLocations) {
            LatLng latLng = new LatLng(waypoint.getLatitude(), waypoint.getLongitude());
            client.getPanoramas(latLng, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONArray photos = response.getJSONArray("photos");
                        for (int i = 0; i < photos.length(); i++) {
                            JSONObject photo = photos.getJSONObject(i);
                            if (!mediaURL.contains(photo.getString("photo_file_url"))){
                            mediaURL.add(photo.getString("photo_file_url"));
                            aaMediaURL.notifyItemInserted(mediaURL.size() - 1);}
                        }
                        Log.d("ROUTE", photos.toString());
                        //aaMediaURL.notifyDataSetChanged();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("RouteDetailMedia", errorResponse.toString());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_route_detail_media, container, false);
        ButterKnife.bind(this, view);

        this.mPage = getArguments().getInt(ARG_PAGE);
        this.rvMedia = (RecyclerView) view.findViewById(R.id.rvMedia);
        rvMedia.setAdapter(aaMediaURL);
        rvMedia.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

}

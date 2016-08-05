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
import com.facebook.seagull.adapters.WaypointsArrayAdapter;
import com.facebook.seagull.decorators.DividerItemDecoration;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteDetailWaypointFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private String routeId;

    private WaypointsArrayAdapter aaWaypoints;
    private ArrayList<Waypoint> waypointsLocations;
    @BindView(R.id.rvWaypoints) RecyclerView rvWaypoints;

    public RouteDetailWaypointFragment() {
        // Required empty public constructor
    }

    public static RouteDetailWaypointFragment newInstance(int page, String routeId) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString("route_id", routeId);
        RouteDetailWaypointFragment fragment = new RouteDetailWaypointFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null ) {
            this.routeId = getArguments().getString("route_id");
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.getInBackground(this.routeId, new GetCallback<Route>() {
                public void done(Route item, ParseException e) {
                    if (e == null) {
                        // "PROBLEM"
                        ParseQuery<Waypoint> query = ParseQuery.getQuery(Waypoint.class);
                        query.whereEqualTo("route", item );
                        query.findInBackground(new FindCallback<Waypoint>() {

                            @Override
                            public void done(List<Waypoint> waypointList,
                                             ParseException e) {
                                if (e == null) {
                                    for (Waypoint waypoint : waypointList) {
                                        waypointsLocations.add(waypoint);
                                    }

                                    aaWaypoints.notifyDataSetChanged();
                                } else {
                                    Log.d("Post retrieval", "Error: " + e.getMessage());
                                }
                            }
                        });

                        // TODO Access route stuff for data population
                    } else {
                        Log.d("RouteDetailMapFragment", e.toString());
                    }
                }
            });
        } else {
            throw new RuntimeException("No route id passed");
        }

        waypointsLocations = new ArrayList<>();
        aaWaypoints = new WaypointsArrayAdapter(getContext(), waypointsLocations);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_route_detail_waypoint, container, false);
        ButterKnife.bind(this, view);

        this.mPage = getArguments().getInt(ARG_PAGE);
        rvWaypoints.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        rvWaypoints.setAdapter(aaWaypoints);
        rvWaypoints.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

}

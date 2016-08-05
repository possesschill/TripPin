package com.facebook.seagull.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.HomeActivity;
import com.facebook.seagull.activities.RouteDetailActivity;
import com.facebook.seagull.adapters.RoutesArrayAdapter;
import com.facebook.seagull.decorators.ItemClickSupport;
import com.facebook.seagull.models.Route;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
    Base fragment for other RouteFeeds
*/

public abstract class RouteFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    public final ArrayList<Route> mRoutes = new ArrayList<>();
    RoutesArrayAdapter aRoutes;
    @BindView(R.id.rvRoutes) RecyclerView rvRoutes;
    @BindView(R.id.rlLoading) RelativeLayout rlLoading;
    public SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aRoutes = new RoutesArrayAdapter(getActivity(), mRoutes, false);
        aRoutes.setOnUserClickListener((HomeActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_route_feed, container, false);
        ButterKnife.bind(this, view);

        // Toolbar for RouteFeed
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.filters_menu);
        toolbar.setTitle("Feed");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_filter:
                        openFilterDialog();
                        break;
                }
                return true;
            }
        });

        ItemClickSupport.addTo(rvRoutes).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent( getActivity() , RouteDetailActivity.class);
                        i.putExtra("route_id", mRoutes.get(position).getObjectId().toString() );
                        startActivity(i);
                    }
                }
        );
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        rvRoutes.setAdapter(aRoutes);
        rvRoutes.setLayoutManager(new LinearLayoutManager(getContext()));
        mPage = getArguments().getInt(ARG_PAGE);

        populateTimeline();
        return view;
    }

    // Create overrides in extended frags: HomeRouteFeedFrag, etc,
    public abstract void populateTimeline();

    public abstract void openFilterDialog();

    public abstract void fetchTimelineAsync(int page);
}

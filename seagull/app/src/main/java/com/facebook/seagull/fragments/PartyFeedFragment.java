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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.PartyRouteActivity;
import com.facebook.seagull.adapters.PartiesArrayAdapter;
import com.facebook.seagull.decorators.DividerItemDecoration;
import com.facebook.seagull.decorators.ItemClickSupport;
import com.facebook.seagull.models.Party;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 Base Fragment
 */
public class PartyFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    public final ArrayList<Party> mParties = new ArrayList<>();
    private PartiesArrayAdapter aParties;
    @BindView(R.id.rvParties) RecyclerView rvParties;
    @BindView(R.id.rlLoading) RelativeLayout rlLoading;
    public SwipeRefreshLayout swipeContainer;

    public PartyFeedFragment() {
        // Required empty public constructor
    }

    public static PartyFeedFragment newInstance(int page) {
        PartyFeedFragment fragment = new PartyFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aParties = new PartiesArrayAdapter(getActivity(), mParties);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_feed, container, false);
        ButterKnife.bind(this, view);

        // Toolbar for Party
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Groups");

        rvParties.setAdapter(aParties);
        rvParties.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        rvParties.setLayoutManager(new LinearLayoutManager(getContext()));

        // on click, open up partyDetails
        ItemClickSupport.addTo(rvParties).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent i = new Intent( getActivity() , PartyRouteActivity.class);
                        i.putExtra("party_id", mParties.get(position).getObjectId() );
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

        mPage = getArguments().getInt(ARG_PAGE);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        populateFeed();
        super.onViewCreated(view, savedInstanceState);
    }

    public void fetchTimelineAsync(int page) {

        populateFeed();

        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }

    private void populateFeed() {

        aParties.clear();
        rvParties.setVisibility(View.GONE);
        rlLoading.setVisibility(ProgressBar.VISIBLE);

        ParseQuery<Party> invited = ParseQuery.getQuery(Party.class);
        invited.whereEqualTo("invited", ParseUser.getCurrentUser());

        ParseQuery<Party> own = ParseQuery.getQuery(Party.class);
        own.whereEqualTo("owner", ParseUser.getCurrentUser());

        List<ParseQuery<Party>> queries = new ArrayList<>();
        queries.add(invited);
        queries.add(own);

        // query for all parties where current user is either invited or is an owner of
        ParseQuery<Party> query = ParseQuery.or(queries);
        query.orderByDescending("createdAt");
        query.setLimit(10);
        query.include("owner");
        query.include("route");
        query.findInBackground(new FindCallback<Party>() {
            @Override
            public void done(List<Party> objects, ParseException e) {
                if (e == null) {
                    aParties.addAll(objects);
                    rlLoading.setVisibility(ProgressBar.INVISIBLE);
                    rvParties.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}

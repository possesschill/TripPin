package com.facebook.seagull.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.ComposeActivity;
import com.facebook.seagull.adapters.CommentArrayAdapter;
import com.facebook.seagull.decorators.DividerItemDecoration;
import com.facebook.seagull.models.Comment;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CommentFeedFragment extends Fragment {

    private static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_ROUTEID = "ARG_ROUTEID" ;
    private String routeId;
    private int mPage;

    private ArrayList<Comment> mComments;
    private CommentArrayAdapter aComments;

    @BindView(R.id.rvComments) RecyclerView rvComments;
    @BindView(R.id.rlLoading) RelativeLayout rlLoading;
    @BindView(R.id.ivCompose) ImageView ivCompose;
    public SwipeRefreshLayout swipeContainer;

    public static final int COMPOSE_REQUEST = 50;
    public static final int NEW_COMMENT = 100;

    public CommentFeedFragment() {
        // Required empty public constructor
    }

    public static CommentFeedFragment newInstance(int page, String routeId) {
        CommentFeedFragment fragment = new CommentFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_ROUTEID, routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeId = getArguments().getString(ARG_ROUTEID);
            mPage = getArguments().getInt(ARG_PAGE);
        }
        mComments = new ArrayList<Comment>();
        aComments = new CommentArrayAdapter(getActivity(), mComments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment_feed, container, false);
        ButterKnife.bind(this, view);


        rvComments.setAdapter(aComments);

        // Add lines between each item and choose vertical layout
        rvComments.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

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

        ivCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ComposeActivity.class);
                i.putExtra("route_id", routeId);
                startActivityForResult(i, COMPOSE_REQUEST);
            }
        });

        return view;
    }

    public void fetchTimelineAsync(int page) {
        aComments.clear();
        populateWithComments();
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        populateWithComments();
        super.onViewCreated(view, savedInstanceState);
    }

    public void populateWithComments() {
        rvComments.setVisibility(View.GONE);
        rlLoading.setVisibility(ProgressBar.VISIBLE);

        // get the comments for that specified route
        ParseQuery<Route> route = ParseQuery.getQuery(Route.class);
        route.getInBackground(routeId, new GetCallback<Route>() {
            @Override
            public void done(Route object, ParseException e) {
                if (e == null) {
                    ParseQuery<Comment> comments = ParseQuery.getQuery(Comment.class);
                    comments.whereEqualTo("route", object);
                    comments.include("user");
                    comments.orderByDescending("createdAt");
                    comments.findInBackground(new FindCallback<Comment>() {
                        @Override
                        public void done(List<Comment> objects, ParseException e) {
                            if (e == null) {
                                aComments.addAll(objects);
                                rlLoading.setVisibility(ProgressBar.INVISIBLE);
                                rvComments.setVisibility(View.VISIBLE);
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NEW_COMMENT && requestCode == COMPOSE_REQUEST) {
            aComments.clear();
            populateWithComments();
        }
    }
}

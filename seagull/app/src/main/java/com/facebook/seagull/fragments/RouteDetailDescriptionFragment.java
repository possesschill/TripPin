package com.facebook.seagull.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.seagull.R;
import com.facebook.seagull.activities.ProfileActivity;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.LabelCheckBox;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailDescriptionFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String ROUTE_ID = "route_id";

    private String routeId;
    private int mPage;
    private LinearLayout labelLayout;
    private TextView tvDescriptionText;

    public RouteDetailDescriptionFragment() {
        // Required empty public constructor
    }

    public static RouteDetailDescriptionFragment newInstance(int page, String routeId) {
        RouteDetailDescriptionFragment fragment = new RouteDetailDescriptionFragment();
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
                        tvDescriptionText.setText(item.getDescription());

                        // LOAD LABEL INFO
                        // TODO progress bar?
                        ParseRelation relation = item.getRelation("label");
                        ParseQuery query = relation.getQuery();
                        query.findInBackground(new FindCallback<Label>() {
                            @Override
                            public void done(List<Label> objects, ParseException e) {
                                if (e == null) {
                                    for (Label label : objects) {
                                        ImageView ivLabelIcon = new ImageView(getContext());
                                        final float scale = getContext().getResources().getDisplayMetrics().density;
                                        int iconDimensions = (int) (30 * scale + 0.5f);
                                        int paddingDimensions = (int) (3 * scale + 0.5f);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconDimensions, iconDimensions);
                                        layoutParams.gravity = Gravity.CENTER_VERTICAL;

                                        switch (label.getText()) {
                                            case "Driving":
                                                ivLabelIcon.setImageResource(R.drawable.car);
                                                ivLabelIcon.setLayoutParams(layoutParams);
                                                ivLabelIcon.setPadding(paddingDimensions, paddingDimensions, paddingDimensions, paddingDimensions);
                                                break;
                                            case "Walking":
                                                ivLabelIcon.setImageResource(R.drawable.bootshort);
                                                ivLabelIcon.setLayoutParams(layoutParams);
                                                ivLabelIcon.setPadding(paddingDimensions, paddingDimensions, paddingDimensions, paddingDimensions);
                                                break;
                                            case "Biking":
                                                ivLabelIcon.setImageResource(R.drawable.bike);
                                                ivLabelIcon.setLayoutParams(layoutParams);
                                                ivLabelIcon.setPadding(paddingDimensions, paddingDimensions, paddingDimensions, paddingDimensions);
                                                break;
                                            case "Other":
                                                break;
                                            default:
                                                ivLabelIcon.setImageResource(0);
                                                TextView tvLabelText = new TextView(getContext());
                                                tvLabelText.setText(label.getText());
                                                tvLabelText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                                tvLabelText.setBackground(getResources().getDrawable(R.drawable.label_rounded_rectangle, null));
                                                tvLabelText.setPadding(2 * paddingDimensions, 0, 2 * paddingDimensions, 0);

                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                params.gravity = Gravity.CENTER_VERTICAL;
                                                tvLabelText.setLayoutParams(params);
                                                labelLayout.addView(tvLabelText);
                                                break;
                                        }

                                        labelLayout.addView(ivLabelIcon);
                                    }

                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        Log.d("RouteDetailMapFragment", e.toString());
                    }
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

        // TODO: CHANGE LAYOUT
        View view = inflater.inflate(R.layout.fragment_route_detail_description, container, false);
        this.mPage = getArguments().getInt(ARG_PAGE);

        labelLayout = (LinearLayout) view.findViewById(R.id.llIcons);
        tvDescriptionText = (TextView) view.findViewById(R.id.tvDescriptionText);

        return view;
    }
}
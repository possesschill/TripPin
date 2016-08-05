package com.facebook.seagull.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.seagull.R;
import com.facebook.seagull.clients.LabelClient;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.LabelCheckBox;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class HomeRouteFeedFragment extends RouteFeedFragment {

    private ArrayList<Label> filters;
    // Filters
    private LabelClient labelMaker;
    private ArrayList<LabelCheckBox> labelObjects;

    public static HomeRouteFeedFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        HomeRouteFeedFragment fragment = new HomeRouteFeedFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        labelMaker = new LabelClient();
        labelObjects = labelMaker.querySearchOptions();
        // Initially begins with no filters
        filters = new ArrayList<>();
    }

    public void populateTimeline() {
        aRoutes.clear();
        rvRoutes.setVisibility(View.GONE);
        rlLoading.setVisibility(ProgressBar.VISIBLE);
        // TODO Edit to make appropriate call to ParseDB and add to array
        // Define the class we would like to query

        // Define our query conditions
        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);

        // Only build OR query if filters exist
        if (!filters.isEmpty()) {
            query = ParseQuery.or(LabelClient.buildOrQuery(filters));
        }
        query.orderByDescending("createdAt");
        query.setLimit(20);
        query.include("user");
        // Execute the find asynchronously
        query.findInBackground(new FindCallback<Route>() {
            public void done(List<Route> itemList, ParseException e) {
                if (e == null) {
                    // Access the array of results here
                    aRoutes.addAll(itemList);
                    rlLoading.setVisibility(ProgressBar.INVISIBLE);
                    rvRoutes.setVisibility(View.VISIBLE);
                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });
    }

    // TODO Send request to ParseDB to fetch the updated data
    @Override
    public void fetchTimelineAsync(int page) {

        populateTimeline();

        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }

    @Override
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

    // Called when filter dialog is closed and new filters exist
    public void reloadWithFilters(ArrayList<Label> mFilters) {
        // Sets filters to list of Labels stored in HomeActivity
        filters = mFilters;
        fetchTimelineAsync(0);
    }
}

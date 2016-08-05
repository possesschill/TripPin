package com.facebook.seagull.clients;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.facebook.seagull.R;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.LabelCheckBox;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kshia on 7/19/16.
 */
public class LabelClient {

    private ArrayList<LabelCheckBox> labels;

    public ArrayList<LabelCheckBox> querySearchOptions() {
        labels = new ArrayList<>();

        // Specify which class to query
        ParseQuery<Label> query = ParseQuery.getQuery(Label.class);
        // Specify the object id
        query.findInBackground(new FindCallback<Label>() {
            @Override
            public void done(List<Label> objects, ParseException e) {
                if (e == null) {
                    for (Label label : objects) {
                        Log.d("LABELS", label.getText());

                        labels.add(new LabelCheckBox(label, null, true));
                    }
                }
                else {
                    e.printStackTrace();
                }
            }
        });

        return labels;
    }

    public void queryAndLoadSearchOptions(final ArrayList<LabelCheckBox> activityLabels, final Context context, final android.support.v7.widget.GridLayout layout) {

        // Specify which class to query
        ParseQuery<Label> query = ParseQuery.getQuery(Label.class);
        // Specify the object id
        query.findInBackground(new FindCallback<Label>() {
            @Override
            public void done(List<Label> objects, ParseException e) {
                if (e == null) {
                    for (Label label : objects) {
                        Log.d("LABELS", label.getText());

                        LabelCheckBox mLabelCheckBox = new LabelCheckBox(label, null, true);
                        activityLabels.add(mLabelCheckBox);
                    }
                    loadSearchOptions(activityLabels, context, layout);
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadSearchOptions(ArrayList<LabelCheckBox> existingLabels, Context context, android.support.v7.widget.GridLayout layout) {
        labels = existingLabels;
        for (LabelCheckBox labelCheckBox : labels) {
            Log.d("LABELS", "Creating boxes for: " + labelCheckBox.getLabel().getText());

            CheckBox cbLabel = new CheckBox(context);
            cbLabel.setText(labelCheckBox.getLabel().getText());
            cbLabel.setTextColor(Color.BLACK);
            cbLabel.setChecked(labelCheckBox.isChecked());
            cbLabel.setButtonTintList(new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled}, //disabled
                            new int[]{android.R.attr.state_enabled} //enabled
                    },
                    new int[] {
                            ContextCompat.getColor(context, R.color.white) //disabled
                            ,ContextCompat.getColor(context, R.color.colorPrimary) //enabled
                    }
            ));
            layout.addView(cbLabel);

            labelCheckBox.setCheckBox(cbLabel);
        }
    }

    public ArrayList<LabelCheckBox> storeSearchOptions() {
        for (LabelCheckBox labelCheckBox : labels) {
            // Need to reset isChecked after dialog is dismissed
            labelCheckBox.setChecked(labelCheckBox.getCheckBox().isChecked());
        }

        return labels;
    }

    public ArrayList<Label> getFilters() {
        ArrayList<Label> onlyLabels = new ArrayList<>();
        for (LabelCheckBox labelCheckBox : labels) {
            if (labelCheckBox.getCheckBox().isChecked()) {
                onlyLabels.add(labelCheckBox.getLabel());
            }
        }
        return onlyLabels;
    }

    // Get all routes that have any of the labels specified in filters (OR)
    public static ArrayList<ParseQuery<Route>> buildOrQuery(ArrayList<Label> filters) {

        // Creates list of queries
        ArrayList<ParseQuery<Route>> queries = new ArrayList<>();

        for (Label orLabel : filters) {
            Log.d("LABELS", "whereEqualTo: " + orLabel.getText());

            // Creates query to include routes with each label
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.whereEqualTo("label", orLabel);
            queries.add(query);
        }

        return queries;
    }
}

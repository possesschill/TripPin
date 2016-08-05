package com.facebook.seagull.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.facebook.seagull.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by satchinc on 7/13/16.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


    private View view;

    public CustomInfoWindowAdapter(Context context) {

        view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {

        if (marker != null
                && marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {

        final EditText etTitle= ((EditText) view.findViewById(R.id.etTitle));



        return view;
    }


}

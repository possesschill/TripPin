package com.facebook.seagull.parsers;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.facebook.seagull.R;
import com.facebook.seagull.models.Route;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by satchinc on 7/18/16.
 */
public class MPolyline {

    public static List<LatLng> decodePoly(String encodedPath) {
        int len = encodedPath.length();

        final List<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    public static Polyline drawPolyLineOnMap(List<LatLng> list, GoogleMap map, Context context) {

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        polyOptions.addAll(list);
        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (3 * scale + 0.5f);
        polyOptions.width(width);
        polyOptions.clickable(true);
        Polyline polyline = map.addPolyline(polyOptions);

        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
        //map.animateCamera(cu);
        return polyline;
    }
    public static ArrayList<Polyline> drawFromRouteArray(ArrayList<Route> nearbyRoutes, GoogleMap map, Context context) {
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (Route route: nearbyRoutes){
            polylines.add(drawPolyLineOnMap(decodePoly(route.getEncodedPolyline()),map, context));
        }
        return  polylines;
    }
}

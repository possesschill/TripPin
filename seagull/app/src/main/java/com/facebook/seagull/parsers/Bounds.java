package com.facebook.seagull.parsers;

import com.facebook.seagull.models.Route;
import com.parse.ParseGeoPoint;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kshia on 7/8/16.
 */
@Parcel
public class Bounds {

    public double neLat;
    public double neLng;
    public double swLat;
    public double swLng;

//    Default constructor for testing
//    public Bounds () {
//        // Using this API call
//        // https://maps.googleapis.com/maps/api/directions/json?origin=Los%20Angeles&destination=San%20Francisco&waypoints=Menlo%20Park&key=AIzaSyAKtIN4hAxlEGOTBbNLB6sB0yayzrcRt50
//        // set test values
//        neLat = 37.7749901;
//        neLng = -118.2416132;
//        swLat = 34.052353;
//        swLng = -122.422491;
//
//    }
    public Bounds(){
    }
    public Bounds(JSONObject boundsJSON){
        try {
            JSONObject neBound = boundsJSON.getJSONObject("northeast");
            JSONObject swBound = boundsJSON.getJSONObject("southwest");
            neLat = neBound.getDouble("lat");
            neLng = neBound.getDouble("lng");
            swLat = swBound.getDouble("lat");
            swLng = swBound.getDouble("lng");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public Bounds(Route route){
        neLat = route.getNeLat();
        neLng = route.getNeLng();
        swLat = route.getSwLat();
        swLng = route.getSwLng();

    }

    public double getNeLat() {
        return neLat;
    }

    public double getNeLng() {
        return neLng;
    }

    public double getSwLat() {
        return swLat;
    }

    public double getSwLng() {
        return swLng;
    }

    @Override
    public String toString() {
        return "NE: (" + neLat + ", " + neLng + ") SW: (" + swLat + ", " + swLng + ")";
    }
    public static ParseGeoPoint getMidPoint(Bounds bounds){
        ParseGeoPoint point= new ParseGeoPoint();
        List<ParseGeoPoint> points= new ArrayList<>();
        points.add(new ParseGeoPoint(bounds.getNeLat(),bounds.getNeLng()));
        points.add(new ParseGeoPoint(bounds.getSwLat(),bounds.getSwLng()));
        return computeCentroid(points);

    }
    public static ParseGeoPoint computeCentroid(List<ParseGeoPoint> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (ParseGeoPoint point : points) {
            latitude += point.getLatitude();
            longitude += point.getLongitude();
        }

        return new ParseGeoPoint(latitude/n, longitude/n);
    }
}

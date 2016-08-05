package com.facebook.seagull.models;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by satchinc on 7/13/16.
 */

public class MarkerPoint {


    public MarkerPoint(Marker marker, LWaypoint lWaypoint ){
        this.marker = marker;
        this.lWaypoint = lWaypoint;
    }
    public MarkerPoint(){
    }
    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }



    public LWaypoint getlWaypoint() {
        return lWaypoint;
    }

    public void setlWaypoint(LWaypoint lWaypoint) {
        this.lWaypoint = lWaypoint;
    }
    private Marker marker;
    public LWaypoint lWaypoint;
}

package com.facebook.seagull.models;

import android.util.Log;

import com.facebook.seagull.parsers.Bounds;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by satchinc on 7/6/16.
 */

@ParseClassName("Route")
public class Route extends ParseObject{

    // Default constructor
    public Route() {
        super();
    }

    public Route(int numWaypoints, Bounds bounds, String name, String description, String encodedPolyline, double aspectRatio, String photoUrl) {
        super();
        setNumPoints(numWaypoints);
        setNeLat(bounds.getNeLat());
        setNeLng(bounds.getNeLng());
        setSwLat(bounds.getSwLat());
        setSwLng(bounds.getSwLng());

        setName(name);
        setDescription(description);
        setEncodedPolyline(encodedPolyline);

        setAspectRatio(aspectRatio);
        setUser(ParseUser.getCurrentUser());
        setPhotoUrl(photoUrl);

        Log.d("ROUTE_ACTIVITY", "numWaypoints: " + numWaypoints + " Bounds: " + bounds.toString());
    }

    //setters
    public void setNumPoints(int numWaypoints){
        put("numWaypoints", numWaypoints);
    }
    public void setName(String routeName){
        put("routeName", routeName);
    }
    public void setDescription(String description) {
        put("description", description);
    }
    public void setEncodedPolyline(String encodedPolyline) {
        put("encodedPolyline", encodedPolyline);
    }
    public void setMidPoint(ParseGeoPoint midPoint){
        put("midPoint", midPoint);
    }

    public void setNeLat(double neLat){
        put("neLat", neLat);
    }
    public void setNeLng(double neLng){
        put("neLng", neLng);
    }
    public void setSwLat(double swLat){
        put("swLat", swLat);
    }
    public void setSwLng(double swLng){
        put("swLng", swLng);
    }

    public void setAspectRatio(double aspectRatio) {
        put("aspectRatio", aspectRatio);
    }
    public void setUser(ParseUser user){
        put("user", user);
    }
    public void setPhotoUrl(String photoUrl) {
        put("photoUrl", photoUrl);
    }

    //getters
    public int getNumWaypoints(){
        return getInt("numWaypoints");
    }
    public String getName() {
        return getString("routeName");
    }
    public String getDescription() {
        return getString("description");
    }
    public String getEncodedPolyline() {
        return getString("encodedPolyline");
    }
    public ParseGeoPoint getMidPoint(){
        return getParseGeoPoint("midPoint");
    }

    public double getNeLat() {
        return getDouble("neLat");
    }
    public double getNeLng() {
        return getDouble("neLng");
    }
    public double getSwLat() {
        return getDouble("swLat");
    }
    public double getSwLng() {
        return getDouble("swLng");
    }

    public double getAspectRatio() {
        return getDouble("aspectRatio");
    }
    public ParseUser getUser(){
        return getParseUser("user");
    }
    public String getPhotoUrl() {
        return getString("photoUrl");
    }

    //extensions
    public void setRating(double rating){//long needs to be a float
        put("rating", rating);
    }
    public double getRating() {
        return getDouble("rating");
    }

    public void setLikes(int likes){
        put("likes", likes);
    }
    public int getLikes() {
        return getInt("likes");
    }

    public void setTimesTaken(int timesTaken){
        put("timesTaken", timesTaken);
    }
    public int getTimesTake(){
        return getInt("timesTaken");
    }

    // Override to allow contains(Route mRoute) to work
    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        try {
            isEqual = fetchIfNeeded().getObjectId().equals(((Route) o).fetchIfNeeded().getObjectId());
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return isEqual;
    }
}

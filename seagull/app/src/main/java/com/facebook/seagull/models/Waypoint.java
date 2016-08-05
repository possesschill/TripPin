package com.facebook.seagull.models;

import android.telecom.Call;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * Created by klimjinx on 7/6/16.
 */

@ParseClassName("Waypoint")
public class Waypoint extends ParseObject {
    // Public default constructor
    public Waypoint() {
        super();
    }

    // Constructor with properties every object must have
    public Waypoint(ParseGeoPoint position, CharSequence name, String description, Number order) {
        super();
        setPosition(position);
        setName(name);
        setDescription(description);
        setOrder(order);
    }

    public String getPid() {
        return  getString("pid");
    }

    public void setPid(String value) {
        put("pid", value);
    }

    public CharSequence getName() {
        return  getString("name");
    }

    public void setName(CharSequence value) {
        put("name", value);
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint("position");
    }

    public void setPosition(ParseGeoPoint coordinates) {
        put("position", coordinates);
    }

    // Can be null as can add place without order in route
    public Number getOrder() {
        return getNumber("order");
    }

    public void setOrder(Number order) {
        put("order", order);
    }

    // parseobject id in string
    public ParseObject getRoute() {
        return getParseObject("route");
    }

    public void setRoute(ParseObject route) {
        put("route", route);
    }

    public Number getRating() {
        return getNumber("rating");
    }

    public void setRating(Number rating) {
        put("rating", rating);
    }

    public String getDescription() {
        return  getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public CharSequence getAddress() {
        return  getString("address");
    }

    // TODO: autopopulate by combining name + current location
    public void setAddress(CharSequence address) {
        put("address", address);
    }

    public Integer getPriceLevel() {
        return getInt("price_level");
    }

    public void setPriceLevel(Integer priceLevel) {
        put("price_level", priceLevel);
    }

    public boolean isGooglePlace() {
        return getBoolean("is_google_place");
    }

    public void setGooglePlace() {
        put("is_google_place", true);
    }

    public String getMyImageUrl() {
        return getString("image_url");
    }

    public void setMyImageUrl(String imageUrl) {
        put("image_url", imageUrl);
    }

    // myViewport
    public ParseGeoPoint getNortheast() {
        return getParseGeoPoint("northeast");
    }

    public void setNortheast(ParseGeoPoint northeast) {
        put("northeast", northeast);
    }

    public ParseGeoPoint getSouthwest() {
        return getParseGeoPoint("southwest");
    }

    public void setSouthwest(ParseGeoPoint southwest) {
        put("southwest", southwest);
    }

    public int getLikesCount() {
        return getInt("likes_count");
    }

    public void setLikesCount(Integer count) {
        put("likes_count", count);
    }

    public static ParseQuery<Waypoint> getQuery() {
        return ParseQuery.getQuery(Waypoint.class);
    }

    // eg: ParseQuery<Waypoint> query = Waypoint.getQuery();

    // BY ID
//    query.getInBackground("aFuEsvjoHt", new GetCallback<Waypoint>() {
//        public void done(Waypoint waypoint, ParseException e) {
//            if (e == null) {
//                // Access data using the `get` methods for the object
//                String body = item.getBody();
//                // Access special values that are built-in to each object
//                String objectId = item.getObjectId();
//                Date updatedAt = item.getUpdatedAt();
//                Date createdAt = item.getCreatedAt();
//                // Do whatever you want with the data...
//                Toast.makeText(DatabaseActivity.this, body, Toast.LENGTH_SHORT).show();
//            } else {
//                // something went wrong
//            }
//        }
//    });

    // By QUERY CONDITIONS
//    query.whereEqualTo("name", "apple"); // choose any waypoint attr that is not null
//// Execute the find asynchronously
//    query.findInBackground(new FindCallback<Waypoint>() {
//        public void done(List<Waypoint> itemList, ParseException e) {
//            if (e == null) {
//                // Access the array of results here
//                String firstItemId = itemList.get(0).getObjectId();
//                Toast.makeText(DatabaseActivity.this, firstItemId, Toast.LENGTH_SHORT).show();
//            } else {
//                Log.d("item", "Error: " + e.getMessage());
//            }
//        }
//    });
    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public static void fromArrayList(ArrayList<LWaypoint> waypointsArray, Route route, SaveCallback cb) {

        for (int i = 0; i < waypointsArray.size(); i++) {
            LWaypoint lwaypoint = waypointsArray.get(i);
            //Populate waypoint object with all info
            //save waypoint object to parse databaseaAZ
            //handle failure for saving
            final Waypoint waypoint = new Waypoint(new ParseGeoPoint(lwaypoint.getPosition().latitude,
                    lwaypoint.getPosition().longitude),
                    lwaypoint.getName(),
                    lwaypoint.getDescription(),
                    lwaypoint.getOrder());
            waypoint.setRoute(route);
            try {
                // Use different callback for last waypoint
                if (i == waypointsArray.size() - 1) {
                    waypoint.saveInBackground(cb);
                }
                else {
                    waypoint.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("saved waypoint", waypoint.getName().toString());
                        }


                    });
                }
            } catch (Exception e) {
                Log.d("Failed saving waypoint", e.toString());
            }

        }
    }

}

package com.facebook.seagull.applications;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.seagull.models.Comment;
import com.facebook.seagull.models.Label;
import com.facebook.seagull.models.Party;
import com.facebook.seagull.models.Photo;
import com.facebook.seagull.models.Route;
import com.facebook.seagull.models.Waypoint;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

public class SeagullApplication extends Application {
    // Debugging switch
    public static final boolean APPDEBUG = false;

    // Debugging tag for the application
    public static final String APPTAG = "Seagull";

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LATITUDE = "latitude";
    public static final String INTENT_EXTRA_LONGITUDE = "longitude";

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";

    // Key for saving the search distance preference
    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private static SharedPreferences preferences;


    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);//These two lines simply provide feedback on facebook insights not really necessary

        ParseObject.registerSubclass(Waypoint.class);
        ParseObject.registerSubclass(Route.class);
        ParseObject.registerSubclass(Label.class);
        ParseObject.registerSubclass(Photo.class);
        ParseObject.registerSubclass(Party.class);
        ParseObject.registerSubclass(Comment.class);
        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("myAppId") // should correspond to APP_ID env variable
                .clientKey("seagull")  // set explicitly unless clientKey is explicitly configured on Parse server
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server("https://seagull1.herokuapp.com/parse/").build());
        ParseFacebookUtils.initialize(this);
        preferences = getSharedPreferences("com.facebook.seagull", Context.MODE_PRIVATE);

    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }


    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).apply();

    }

}

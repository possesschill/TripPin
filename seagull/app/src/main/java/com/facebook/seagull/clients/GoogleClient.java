package com.facebook.seagull.clients;

import android.content.Context;
import android.util.Log;

import com.facebook.seagull.R;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by kshia on 7/6/16.
 */


public class GoogleClient {
    public void getDirections(RequestParams params, AsyncHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://maps.googleapis.com/maps/api/directions/json";
        Log.d("DIRECTIONS_CLIENT", url + "?" + params);

        //?origin=Toronto&destination=Montreal&key=YOUR_API_KEY
        client.get(url, params, handler);
    }

    public void getStaticMap(RequestParams params, AsyncHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://maps.googleapis.com/maps/api/staticmap";
        Log.d("CLIENT", url + "?" + params);

        client.get(url, params, handler);
    }

    public void getPlaceInfo(Context context, LatLng latlng, JsonHttpResponseHandler handler){
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://maps.googleapis.com/maps/api/geocode/json";
        RequestParams params = new RequestParams();
        params.put("latlng", latlng.latitude+","+ latlng.longitude);
        params.put("key", context.getResources().getString(R.string.google_maps_key));
        Log.d("DIRECTIONS_CLIENT", params.toString());
        client.setTimeout(3500);
        client.get(url, params, handler);
    }
}

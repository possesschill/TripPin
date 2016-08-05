package com.facebook.seagull.clients;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.google.maps.android.SphericalUtil.computeOffset;

/**
 * Created by klimjinx on 7/19/16.
 */
public class PanoramioClient {

    public void getPanoramas(LatLng latlng, JsonHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://www.panoramio.com/map/get_panoramas.php";

//http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&minx=-180&miny=-90&maxx=180&maxy=90&size=medium&mapfilter=true

        RequestParams params = new RequestParams();
        params.put("set", "public"); //use "full" for all, public for popular
        params.put("from", 0);
        params.put("to", 10); // get the latest 10
        params.put("mapfilter", true); // takes into account the location and tries to avoid of returning photos

        // x is longitude!

        // Each degree of latitude is approximately 69 miles
        // 10.35 miles E and W
        params.put("miny", latlng.latitude - 0.15);
        params.put("maxy", latlng.latitude + 0.15);

        LatLng rightOffset = computeOffset(latlng, 20000.0, 90.0); // 20 km E
        LatLng leftOffset = computeOffset(latlng, 20000.0, 270.0); // 20 km W

        params.put("minx", leftOffset.longitude);
        params.put("maxx", rightOffset.longitude);

        client.get(url, params, handler);
    }

}

package com.facebook.seagull.clients;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;

/**
 * Created by satchinc on 7/12/16.
 */
public class GraphClient extends GraphRequest {

    public static GraphRequest getInfoClient(String fields, Callback cb) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        return new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", parameters, HttpMethod.GET, cb);
    }

    public static GraphRequest getOtherUserClient(String fields, String userId, Callback cb) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        return new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId, parameters, HttpMethod.GET, cb);
    }
    public static GraphRequest getUserFBID(Callback cb){
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        GraphRequest request = new GraphRequest((AccessToken.getCurrentAccessToken()), "/me", parameters, HttpMethod.GET, cb);
        return request;
    }

}

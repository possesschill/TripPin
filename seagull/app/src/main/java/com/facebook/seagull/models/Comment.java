package com.facebook.seagull.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by klimjinx on 7/29/16.
 */

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public Comment() {}

    public String getMessage() {return getString("message");}

    public void setMessage(String message) {put("message", message);}

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public Route getRoute() {
        return (Route) getParseObject("route");
    }

    public void setRoute(Route route) {
        put("route", route);
    }

}

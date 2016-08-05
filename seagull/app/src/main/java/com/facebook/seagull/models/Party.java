package com.facebook.seagull.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by satchinc on 7/21/16.
 */
@ParseClassName("Party")
public class Party extends ParseObject {

    public Party(){
        super();
    }

    public Party(String time, String date, String details, String name, Route route, ParseUser owner) {
        super();
        setTime(time);
        setDate(date);
        setDetails(details);
        setName(name);
        setRoute(route);
        setOwner(owner);

    }

    //setters
    public void setTime(String time){//Use SimpleTimeFormat to convert
        put("time", time);
    }
    public void setDate(String date){//Use SimpleDateFormat
        put("date", date);
    }
    public void setDetails(String details){
        put("details", details);
    }
    public void setName(String name){
        put("name", name);
    }
    public void setRoute(ParseObject route){
        put("route", route);
    }
    public void setOwner(ParseUser owner) {
        put("owner", owner);
    }

    //getter
    public String getTime(){
        return getString("time");
    }
    public String getDate(){
        return getString("date");
    }
    public String getDetails(){
        return getString("details");
    }
    public String getName(){
        return getString("name");
    }
    public ParseObject getRoute(){
        return getParseObject("route");
    }
    public ParseUser getOwner() {return getParseUser("owner");}



}

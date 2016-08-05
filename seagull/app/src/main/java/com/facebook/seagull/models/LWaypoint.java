package com.facebook.seagull.models;

import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by satchinc on 7/14/16.
 */

public class LWaypoint implements Parcelable {

public LWaypoint(){}

    private LWaypoint(android.os.Parcel in) {
        position = in.readParcelable(LatLng.class.getClassLoader());
        order = in.readDouble();
        name = in.readString();
        description = in.readString();
    }

    public static final Creator<LWaypoint> CREATOR = new Creator<LWaypoint>() {
        @Override
        public LWaypoint createFromParcel(android.os.Parcel in) {
            return new LWaypoint(in);
        }

        @Override
        public LWaypoint[] newArray(int size) {
            return new LWaypoint[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public LatLng position;
    public double order;
    public String name;
    public String description;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeParcelable(position, flags);
        dest.writeDouble(order);
        dest.writeString(name);
        dest.writeString(description);

    }
}

package com.facebook.seagull.models;

import java.util.ArrayList;

// Refer to http://android-pratap.blogspot.com.ng/2015/12/horizontal-recyclerview-in-vertical.html
// for use of multi-hori views in a vert recycler-view

/**
 * Created by klimjinx on 7/25/16.
 */

public class SectionRouteModel {

    private String headerTitle;
    private ArrayList<Route> allRoutesInSection;

    public SectionRouteModel() {
    }

    public SectionRouteModel(String headerTitle, ArrayList<Route> allRoutesInSection) {
        this.headerTitle = headerTitle;
        this.allRoutesInSection = allRoutesInSection;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }


    public java.util.ArrayList<Route> getAllRoutesInSection() {
        return allRoutesInSection;
    }

    public ArrayList<Route> getAllItemsInSection() {
        return allRoutesInSection;
    }

    public void setAllItemsInSection(ArrayList<Route> allRoutesInSection) {
        this.allRoutesInSection = allRoutesInSection;
    }

}
package com.facebook.seagull.models;

import com.parse.ParseUser;

/**
 * Created by satchinc on 7/25/16.
 */
public class FriendCheck {
    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean bool) {
        isSelected = bool;
    }

    private ParseUser user;
    private Boolean isSelected;

    public FriendCheck(ParseUser user) {
        isSelected = false;
        this.user = user;
    }
}

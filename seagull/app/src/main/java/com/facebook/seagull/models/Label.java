package com.facebook.seagull.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by kshia on 7/14/16.
 */

@ParseClassName("Label")
public class Label extends ParseObject {

    public Label() {
        super();
    }

    public Label(String text) {
        super();
        setText(text);
    }

    public void setText(String text) {
        put("text", text);
    }

    public String getText() {
        return getString("text");
    }

}

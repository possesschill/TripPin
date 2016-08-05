package com.facebook.seagull.models;

import android.widget.CheckBox;

// Local pair for Label and CheckBox
public class LabelCheckBox {
    private Label label;
    private CheckBox checkBox;
    private boolean isChecked;

    public LabelCheckBox(Label mLabel, CheckBox mCheckBox, boolean checked) {
        label = mLabel;
        checkBox = mCheckBox;
        isChecked = checked;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }


    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

package com.facebook.seagull.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.facebook.seagull.R;
import com.facebook.seagull.fragments.CommentFeedFragment;
import com.facebook.seagull.fragments.RouteDetailDescriptionFragment;
import com.facebook.seagull.fragments.RouteDetailWaypointFragment;
import com.facebook.seagull.fragments.RouteDetailMapFragment;
import com.facebook.seagull.fragments.RouteDetailMediaFragment;

/**
 * Created by klimjinx on 7/15/16.
 */
public class RouteDetailFragmentPagerAdapter  extends SmartFragmentStatePagerAdapter {
    public final static int DESCRIPTION_TAB = 0;
    public final static int MEDIA_TAB = 1;
    public final static int WAYPOINT_TAB = 2;
    public final static int COMMENT_TAB = 3;

    // TODO change icons
    private int[] imageResId = {
            R.drawable.ic_list,
            R.drawable.ic_gallery,
            R.drawable.compass,
            R.drawable.ic_forum
    };

    final int PAGE_COUNT = 4;

    // TODO change according to Tab names
    private static String tabTitles[] = new String[] { "Description", "Media", "Waypoints", "Comments"};
    private Context context;
    private String routeId;

    public RouteDetailFragmentPagerAdapter(FragmentManager fm, Context context, String routeId) {
        super(fm);
        this.context = context;
        this.routeId = routeId;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        // TODO change according to fragments
        switch (position) {
            case DESCRIPTION_TAB:
                return RouteDetailDescriptionFragment.newInstance(0, this.routeId);
            case MEDIA_TAB:
                return RouteDetailMediaFragment.newInstance(1, this.routeId);
            case WAYPOINT_TAB:
                return RouteDetailWaypointFragment.newInstance(2, this.routeId);
            case COMMENT_TAB:
                return CommentFeedFragment.newInstance(3, this.routeId);
            default:
                return RouteDetailMapFragment.newInstance(0, this.routeId);
        }
    }

    // Generate title based on item position and set the title
    @Override
    public CharSequence getPageTitle(int position) {
        Drawable image = ContextCompat.getDrawable(context, imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    public static String getTitle(int position) {
        return tabTitles[position];
    }
}

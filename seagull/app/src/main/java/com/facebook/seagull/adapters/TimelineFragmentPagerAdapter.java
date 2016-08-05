package com.facebook.seagull.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.facebook.seagull.fragments.HomeRouteFeedFragment;
import com.facebook.seagull.R;

/**
 * Created by klimjinx on 6/27/16.
 */
public class TimelineFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

    public final static int HOME_TAB = 0;
    public final static int MENTIONS_TAB = 1;
    public final static int MOMENTS_TAB = 2;

    // TODO change icons
    private int[] imageResId = {
            R.drawable.ic_navigate,
            R.drawable.ic_list,
            R.drawable.ic_profile,
    };

    final int PAGE_COUNT = 3;

    // TODO change according to Tab names
    private static String tabTitles[] = new String[] { "Home", "Mentions", "Moments" };
    private Context context;

    public TimelineFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
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
//            case HOME_TAB:
//                return HomeTimelineFragment.newInstance(0);
//            case MENTIONS_TAB:
//                return MentionsTimelineFragment.newInstance(1);
////            case MOMENTS_TAB:
////                return MomentsTimelineFragment.newInstance(2);
            default:
                return HomeRouteFeedFragment.newInstance(0);
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

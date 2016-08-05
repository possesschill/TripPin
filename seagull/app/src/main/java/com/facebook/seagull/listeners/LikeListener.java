package com.facebook.seagull.listeners;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.seagull.R;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by kshia on 7/26/16.
 */
public class LikeListener implements View.OnClickListener{

    Context mContext;
    ImageView btnLike;
    TextView tvNumLikes;
    Route route;

    public LikeListener(Context context, ImageView btn, Route mRoute, TextView num) {
        mContext = context;
        btnLike = btn;
        route = mRoute;
        tvNumLikes = num;
    }

    @Override
    public void onClick(View v) {

        // Get whether currently liked or not
        boolean isLiked = (Boolean) btnLike.getTag();
        ParseRelation<ParseUser> relation = route.getRelation("like");

        // Already liked so now unliked
        if (isLiked) {
            relation.remove(ParseUser.getCurrentUser());
            btnLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heartgray, null));
        }

        // Not liked so now like
        else {
            relation.add(ParseUser.getCurrentUser());
            btnLike.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heartred, null));
        }

        // Set tag to opposite of original tag
        btnLike.setTag(!isLiked);

        // Save route with new "like" relation values
        route.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (tvNumLikes != null) {
                        updateLikes();
                    }
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    // Query database to see if current user liked route
    public static void queryLiked (final Context context, Route myRoute, final ImageView heart) {
        ParseRelation relation = myRoute.getRelation("like");
        ParseQuery query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {

                    // Check all users who liked to route to see if it includes the current user
                    for (ParseUser user : users) {

                        // Set to red heart if liked, also set tag to show liked
                        if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            heart.setImageDrawable(context.getResources().getDrawable(R.drawable.heartred, null));
                            heart.setTag(true);
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateLikes() {
        // LOAD LIKE INFO
        ParseRelation relationLike = route.getRelation("like");
        ParseQuery queryLike = relationLike.getQuery();
        queryLike.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    tvNumLikes.setText(Integer.toString(objects.size()));

                    Log.d("RouteDetailActivity", "Likes: " + objects.size());
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}

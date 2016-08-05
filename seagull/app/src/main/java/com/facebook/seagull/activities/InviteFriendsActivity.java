package com.facebook.seagull.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.seagull.R;
import com.facebook.seagull.adapters.FriendListAdapter;
import com.facebook.seagull.models.FriendCheck;
import com.facebook.seagull.models.Party;
import com.facebook.seagull.models.Route;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsActivity extends AppCompatActivity {
    ListView lvFriends;
    List<ParseUser> friends;
    FriendListAdapter friendAdapter;
    List<FriendCheck> friendsCheckList;
    Party party;
    ArrayList<ParseUser> invitedFriends;
    String routeId;
    Route route;
    String name;
    String date;
    String time;
    String description;
    Toolbar toolbar;
    InviteFriendsActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        route = null;
        party= null;
        setContentView(R.layout.activity_invite_friends);
        context = this;

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Make sure the toolbar exists and add back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
        getSupportActionBar().setTitle("Invite people");

        name = getIntent().getStringExtra("name");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        description = getIntent().getStringExtra("description");
        routeId = getIntent().getStringExtra("route_id");
        //exception where routeId is blank could be handled
        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
        query.getInBackground(routeId, new GetCallback<Route>() {
            @Override
            public void done(Route object, ParseException e) {
                Log.d("PartyRoute", object.getName());
                route = object;
                party  = new Party(time,date, description ,name, route, ParseUser.getCurrentUser());
            }
        });
        //    public Party(String time, String date, String details, String name, Route route) {

        lvFriends = (ListView) findViewById(R.id.lvFriends);
        friends = new ArrayList<>();
        friendsCheckList = new ArrayList<>();
        invitedFriends = new ArrayList<>();
        friendAdapter = new FriendListAdapter(this, friendsCheckList);
        lvFriends.setAdapter(friendAdapter);
        final GraphRequest request = GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray array, GraphResponse response) {
                        Log.d("Friends", response.toString());
                        // get the user fbid who also installed app
                        JSONObject result = response.getJSONObject();
                        try {
                            JSONArray data = result.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++ ) {
                                // Define the class we would like to query
                                ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
                                query.whereEqualTo("fbid", data.getJSONObject(i).getString("id") );
                                query.findInBackground(new FindCallback<ParseUser>() {
                                    public void done(List<ParseUser> itemList, ParseException e) {
                                        Log.d("Friends", itemList.toString());
                                        if (e == null) {
                                            for(ParseUser friend : itemList){
                                                //friendsCheckList.add(new FriendCheck(friend));
                                                friendAdapter.add(new FriendCheck(friend));
                                                Log.d("Friends", "Loops");
                                            }
                                                if (!friendsCheckList.isEmpty()) {
                                                    friendAdapter.notifyDataSetChanged();
                                                    for (ParseUser parseUser : itemList) {
                                                        Log.d("Friends", parseUser.getString("last_name"));
                                                    }
                                                }
                                        } else {
                                            Log.d("item", "Error: " + e.getMessage());
                                        }
                                    }
                                });
                            }
//                        horizontalAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "installed");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveParty(View v) {

                ArrayList<FriendCheck> friends =  new ArrayList<>();
                friends.addAll(friendAdapter.getFriendChecklist());
        Log.d("Party", "Size : " + Integer.toString(friends.size()));
                for(int i = 0; i < friends.size(); i++){
                    //get the list
                    //check if selected
                  if(friends.get(i).getSelected()){
                        invitedFriends.add(friends.get(i).getUser());
                  }
                    //add to list
                    //make query
                    //create relations
                    //create party parse object
                    //fix scrolling bug before it start

                }//end for
                createPartyObject();


    }

    private void createPartyObject() {
        if(party==null) {
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.getInBackground(routeId, new GetCallback<Route>() {
                @Override
                public void done(Route object, ParseException e) {
                    if(e==null){
                        Log.d("PartyRoute", object.getName());
                        route = object;
                        party  = new Party(time,date, description ,name, route, ParseUser.getCurrentUser());
                    }
                    else{
                        Snackbar locSnackbar= Snackbar
                                .make(getCurrentFocus(), "Network Problems", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener(){
                                    @Override
                                    public void onClick(View v) {

                                      networkErrorSnackbar(1);
                                    }
                                });
                        locSnackbar.show();
                    }
                }
            });
        }
        setRelations(party);

    }

    private void setRelations(final Party party) {
        ParseRelation<ParseObject> relation = party.getRelation("invited");
        for(ParseUser friend : invitedFriends){
            relation.add(friend);
            Log.d("Party", friend.getString("first_name") + " is related");
        }
        party.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Log.d("Party", "Success?");

                    Intent i = new Intent(InviteFriendsActivity.this, PartyRouteActivity.class);
                    i.putExtra("party_id", party.getObjectId());
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(context, (Toolbar) toolbar, "toolbar");
                    finish();
                }
                else{
                    networkErrorSnackbar(2);
                }

                //PROGESS BAR NECESSARY
            }
        });

    }

    private void networkErrorSnackbar(int errorCode){
        switch (errorCode){
            case 1 : createPartyObject();
                break;
            case 2 : setRelations(party);
                break;
            default : finish();//if crashes backtrack to details page
                break;
        }
    }
}

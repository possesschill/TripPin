package com.facebook.seagull.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.seagull.R;
import com.facebook.seagull.adapters.InvitedFriendListAdapter;
import com.facebook.seagull.decorators.ItemClickSupport;
import com.facebook.seagull.models.Party;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PartyRouteActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tvPartyName)
    TextView tvPartyName;
    @BindView(R.id.ivMain)
    ImageView ivMain;
    @BindView(R.id.lvInvitedFriends)
    RecyclerView lvFriends;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvTime)
    TextView tvTime;
    private Party party;
    @BindView(R.id.tvDetails)
    TextView tvDetails;

//   @BindView(R.id.fabParty)
//    FloatingActionButton fab;

    private ArrayList<ParseUser> friendsList;
    private InvitedFriendListAdapter friendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_route);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Party Trip");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
//        fab.bringToFront();

        friendsList = new ArrayList<>();
        friendAdapter = new InvitedFriendListAdapter(this, friendsList);

        lvFriends.setLayoutManager(new LinearLayoutManager(PartyRouteActivity.this, LinearLayoutManager.VERTICAL, false));
        lvFriends.setAdapter(friendAdapter);
        lvFriends.setFocusable(false);

        String partyId = getIntent().getStringExtra("party_id");
        ParseQuery<Party> query = ParseQuery.getQuery(Party.class);
        query.include("owner");
        query.include("route");
        query.getInBackground(partyId, new GetCallback<Party>() {
                    @Override
                    public void done(Party object, ParseException e) {
                        party = object;
                        populatePartyFields();
                        queryForFriends();
                    }
                });

        ItemClickSupport.addTo(lvFriends).setOnItemClickListener(
            new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    final ParseUser friend = friendsList.get(position);
                    if (friend.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                        Intent i = new Intent(PartyRouteActivity.this, HomeActivity.class);
                        i.putExtra("profile", "personal");
                        startActivity(i);
                    } else{
                        Intent i = new Intent(PartyRouteActivity.this, ProfileActivity.class);
                        i.putExtra("id", friend.getObjectId());
                        startActivity(i);
                    }
                }
            }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.party_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case R.id.action_route :
                Intent i = new Intent(PartyRouteActivity.this, RouteDetailActivity.class);
                i.putExtra("route_id", party.getRoute().getObjectId().toString());
                startActivity(i);
                break;
            case android.R.id.home :
                finish(); // close this activity and return to preview activity (if there is any)
                break;
            }
        return super.onOptionsItemSelected(item);
    }

    private void populatePartyFields() {
        tvDate.setText(party.getString("date"));
        tvTime.setText(party.getString("time"));
        tvDetails.setText(party.getString("details"));
        tvPartyName.setText(party.getName());
                Glide.with(getApplicationContext()).load(party.getParseObject("route").get("photoUrl")).into(ivMain);
    }

    private void queryForFriends() {
        ParseRelation relation = party.getRelation("invited");
        ParseQuery query = relation.getQuery();
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {
                Log.d("Party" ,objects.toString());
            }

            @Override
            public void done(Object o, Throwable throwable) {
                ArrayList<ParseUser> array = (ArrayList<ParseUser>) o;
                array.add(party.getOwner());
                friendAdapter.setOwner(party.getOwner());
                Log.d("Party", o.toString());
                for(ParseUser parseUser : array){
                    friendsList.add(parseUser);
                }
                friendAdapter.notifyDataSetChanged();
              //populateFields();
            }
        });
    }
}

package com.facebook.seagull.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.models.Route;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PartyCreateActivity extends AppCompatActivity {

    SimpleDateFormat pickedDate = new SimpleDateFormat("EEE, MMM d, ''yy");
    SimpleDateFormat pickedTime = new SimpleDateFormat("h:mm a");
    TextView tvDate;
    TextView tvTime;
    EditText etDetails;
    EditText etName;
    Calendar now;
    String routeId;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_create);
         toolbar = (Toolbar) findViewById(R.id.toolbar);
//         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // Sets the Toolbar to act as the ActionBar for this Activity window.

        // Make sure the toolbar exists and add back button
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_light_24dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Enter Party Information");

        //Get Route ID from Intent

        routeId = getIntent().getStringExtra("route_id");

        ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
        query.getInBackground(routeId, new GetCallback<Route>() {
            @Override
            public void done(Route object, ParseException e) {
                Log.d("PartyRoute", object.getName());
            }
        });

        etName = (EditText) findViewById(R.id.etName);
        etDetails = (EditText) findViewById(R.id.etDetails);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTime = (TextView) findViewById(R.id.tvTime);
        String date = pickedDate.format(Calendar.getInstance().getTime());
        String dateForTime = pickedTime.format(Calendar.getInstance().getTime());
        tvDate.setText(date);
        tvTime.setText(dateForTime);

        now = Calendar.getInstance();

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


    public void showDatePickerDialog(View v) {
        final DatePickerDialog datePickerDialog = new DatePickerDialog(PartyCreateActivity.this,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String frmDate = pickedDate.format(c.getTime());
                    tvDate.setText(frmDate);
            }
        },now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    public void showTimePickerDialog(View view) {
        final TimePickerDialog timePickerDialog = new TimePickerDialog(PartyCreateActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Date  date = new Date(12,12, 12, hourOfDay, minute, 0);
                String frmDate = pickedTime.format(date);
                Log.d("Party", frmDate);
                tvTime.setText(frmDate);
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    public void saveDetails(View view) {
        if (etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(PartyCreateActivity.this, InviteFriendsActivity.class);
            i.putExtra("name", etName.getText().toString());
            i.putExtra("date", tvDate.getText().toString());//PASSING DATE AND TIME AS STRINGS DOES NOT ALLOW EDITING, EASILY
            i.putExtra("time", tvTime.getText().toString());//CONVERT TO DATE FORMAT TO STORE IF EDITING FUNCTIONALITY INTENDED
            i.putExtra("description", etDetails.getText().toString());
            i.putExtra("route_id", routeId);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, (Toolbar) toolbar, "toolbar");
            startActivity(i);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

            finish();//if crashes backtracks to route page
        }
    }
}


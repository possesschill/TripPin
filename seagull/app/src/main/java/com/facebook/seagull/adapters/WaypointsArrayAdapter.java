package com.facebook.seagull.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.zetterstrom.com.forecast.ForecastClient;
import android.zetterstrom.com.forecast.ForecastConfiguration;
import android.zetterstrom.com.forecast.models.Forecast;

import com.facebook.seagull.R;
import com.facebook.seagull.activities.StreetviewWaypointActivity;
import com.facebook.seagull.models.Waypoint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    Recyclerview adapter when populating a feed with waypoints
 */

// view holder pattern for every item;
public class WaypointsArrayAdapter extends RecyclerView.Adapter<WaypointsArrayAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private ArrayList<Waypoint> amWaypoints;
    // Store the context for easy access
    private Context mContext;
    // Font for customization
    private Typeface font;

    // Pass in the waypoint array into the constructor
    public WaypointsArrayAdapter(Context context, ArrayList<Waypoint> tweets) {
        this.amWaypoints = tweets;
        this.mContext = context;
        //font = Typeface.createFromAsset(context.getAssets(), "fonts/GothamNarrow-Book.otf");
    }

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder{
            // holder contain member variable for any view that is rendered in a row

            // TODO customize item_waypoint.xml
            private TextView tvName;
            private TextView tvDescription;
            private TextView tvNumber;
            private Button btnPanorama;
            private Button btnWeather;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                // TODO findViewById according to item_waypoint.xml
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
                tvNumber = (TextView) itemView.findViewById(R.id.tvNumber);
                btnPanorama = (Button) itemView.findViewById(R.id.btnPanorama);
                btnWeather = (Button) itemView.findViewById(R.id.btnWeather);

                // tvUsername.setTypeface(font);
            }
        }

        // Usually involves inflating a layout from XML and returning the holder
        @Override
        public WaypointsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.item_waypoint, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(final WaypointsArrayAdapter.ViewHolder viewHolder, int position) {
            // Get the data model based on position
            final Waypoint waypoint = amWaypoints.get(position);
            final int pos = position;

            final Button btnPanorama = viewHolder.btnPanorama;
            final Button btnWeather = viewHolder.btnWeather;

            btnPanorama.setOnClickListener(new View.OnClickListener() {
                // TODO make a call to ParseDB depending on action made
                @Override
                public void onClick(View v) {
                    // setup according to ParseDB post/create/put
                    //  Don't forget to notifyItemChanged(pos)
                    Intent i = new Intent (v.getContext(), StreetviewWaypointActivity.class);
                    i.putExtra("name", waypoint.getName());
                    i.putExtra("latitude", waypoint.getPosition().getLatitude() );
                    i.putExtra("longitude", waypoint.getPosition().getLongitude() );
                    v.getContext().startActivity(i);
                }
            });

           btnWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showWeatherDialog(waypoint);
                }
            });

            // number waypoints with starting with index 1
            viewHolder.tvNumber.setText(Integer.toString(position + 1));

            // Set item views based on your views and data model
            viewHolder.tvName.setText(waypoint.getName());

            if(waypoint.getDescription().isEmpty()) {
                viewHolder.tvDescription.setVisibility(View.GONE);
            }
            else {
                viewHolder.tvDescription.setText(waypoint.getDescription());
            }
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return amWaypoints.size();
        }

        // Clean all elements of the recycler
        public void clear() {
            amWaypoints.clear();
            notifyDataSetChanged();
        }

        // Add a list of items
        public void addAll(List<Waypoint> list) {
            amWaypoints.addAll(list);
            notifyDataSetChanged();
        }

    public void showWeatherDialog(Waypoint waypoint) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_weather, null);
        dialogBuilder.setView(dialogView);

        final TextView tvTemperature = (TextView) dialogView.findViewById(R.id.tvTemperature);
        final TextView tvConditions = (TextView) dialogView.findViewById(R.id.tvConditions);
        final ImageView ivCurCond = (ImageView) dialogView.findViewById(R.id.ivCurCond);

        // DarkSky Forecast Api Call
        ForecastConfiguration configuration =
                new ForecastConfiguration.Builder("e2a422ceac2d0663eef9bf130729b97d")
                        .setCacheDirectory(mContext.getCacheDir())
                        .build();
        ForecastClient.create(configuration);

        double latitude = waypoint.getPosition().getLatitude();
        double longitude = waypoint.getPosition().getLongitude();
        ForecastClient.getInstance()
                .getForecast(latitude, longitude, new Callback<Forecast>() {
                    @Override
                    public void onResponse(Call<Forecast> forecastCall, Response<Forecast> response) {
                        if (response.isSuccessful()) {
                            Forecast forecast = response.body();
                            int curTemp = forecast.getCurrently().getTemperature().intValue();
                            String strCurTemp = Integer.toString(curTemp) + "\u00B0F";
                            // Set the weather conditions

                            tvTemperature.setText(strCurTemp);
                            tvConditions.setText(forecast.getCurrently().getSummary());
                            setIcon(forecast.getCurrently().getIcon().getText(), ivCurCond);
                            dialogBuilder.create().show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Forecast> forecastCall, Throwable t) {

                    }
                });


        dialogBuilder.setTitle(waypoint.getName());
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }
        });
    }

    public void setIcon(String condition, ImageView icon) {
        switch (condition) {
            case "partly-cloudy-day":
                icon.setImageResource(R.drawable.partly_cloudy_day);
                break;
            case "partly-cloudy-night":
                icon.setImageResource(R.drawable.partly_cloudy_night);
                break;
            case "clear-day":
                icon.setImageResource(R.drawable.clear_day);
                break;
            case "clear-night":
                icon.setImageResource(R.drawable.clear_night);
                break;
            case "rain":
                icon.setImageResource(R.drawable.rain);
                break;
            case "snow":
                icon.setImageResource(R.drawable.snowflake);
                break;
            case "sleet":
                icon.setImageResource(R.drawable.sleet);
                break;
            case "wind":
                icon.setImageResource(R.drawable.wind);
                break;
            case "fog":
                icon.setImageResource(R.drawable.fog);
                break;
            case "cloudy":
                icon.setImageResource(R.drawable.cloudy);
                break;
            case "hail":
                icon.setImageResource(R.drawable.hail);
                break;
            case "thunderstorm":
                icon.setImageResource(R.drawable.thunderstorm);
                break;
            default:
                icon.setImageResource(R.drawable.planet_earth);
                break;
        }
    }
}

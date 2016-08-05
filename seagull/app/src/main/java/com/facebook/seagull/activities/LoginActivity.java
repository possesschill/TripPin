package com.facebook.seagull.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.seagull.R;
import com.facebook.seagull.clients.GraphClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    //CallbackManager callbackManager;
    ParseUser mUser = new ParseUser();
    GraphClient graphClient;
    JSONObject authData;

    public void populateUserData() {
        GraphClient client = new GraphClient();
        // note syntax for specifying additional parameters for picture size
        GraphRequest request = client.getInfoClient("first_name,last_name,picture.type(large)", new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    Log.d("GRAPH_REQUEST", response.toString());
                    JSONObject object = response.getJSONObject();
                    String firstName = object.getString("first_name");
                    String lastName = object.getString("last_name");
                    String profilePicURL = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    String name = firstName + " " + lastName;
                    ParseUser.getCurrentUser().put("first_name", firstName);
                    ParseUser.getCurrentUser().put("last_name", lastName);
                    ParseUser.getCurrentUser().put("picture", profilePicURL);
                    Log.d("LoginActivity", ParseUser.getCurrentUser().toString());
                    ParseUser.getCurrentUser().saveInBackground();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.executeAsync();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        graphClient = new GraphClient();
        mUser = ParseUser.getCurrentUser();
        if (mUser != null) {
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        } else {
            //run the log in sequence
        }

        Button button = (Button) findViewById(R.id.login);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> permissions = new ArrayList();
                permissions.add("email");
                permissions.add("public_profile");
                permissions.add("user_friends");

                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions,
                    new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException err) {
                            if (err != null) {
                                Log.d("MyApp", "Uh oh. Error occurred" + err.toString());
                            } else if (user == null) {
                                Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                            } else if (user.isNew()) {
                                if (user != null) {
                                    try {
                                        authData = ParseUser.getCurrentUser().fetchIfNeeded().getJSONObject("authData");
                                        try {
                                            ParseUser.getCurrentUser().put("fbid", authData.getJSONObject("facebook").getString("id"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    populateUserData();
                                    ParseUser.getCurrentUser().saveInBackground();
                                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(i);
                                    finish();

                                }
                            } else {

                                if (user != null) {
                                    try {
                                        authData = ParseUser.getCurrentUser().fetchIfNeeded().getJSONObject("authData");
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    ParseUser.getCurrentUser().put("fbid", authData.getJSONObject("facebook").getString("id"));
                                    Log.d("id", authData.getJSONObject("facebook").getString("id"));
                                    populateUserData();
                                    ParseUser.getCurrentUser().saveInBackground();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("MyApp", "User logged in through Facebook!");
                                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }

                    });
            }
        });
    }


    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.facebook.seagull", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", sign);
            }
            // Log.d("KeyHash:", sign);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
        String profileUri = Profile.getCurrentProfile().getProfilePictureUri(400, 400).toString();
        Log.d("uri", profileUri);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

    }
}

package com.facebook.seagull.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.seagull.R;
import com.facebook.seagull.fragments.CommentFeedFragment;
import com.facebook.seagull.models.Comment;
import com.facebook.seagull.models.Route;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ComposeActivity extends AppCompatActivity {

    @BindView(R.id.etComment) EditText etComment;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private Comment comment;
    private String routeId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);
        routeId = getIntent().getStringExtra("route_id");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Comment");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_light_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.send) {
            postComment();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void postComment() {
        //send tweet
        String toComment = etComment.getText().toString();
        if (toComment.trim().isEmpty()) {
            Toast.makeText(getApplicationContext() ,"Sorry, message cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            createCommentObject();
        }
    }

    private void createCommentObject() {
        progressDialog = new ProgressDialog(ComposeActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.comment_dialog_text));
        progressDialog.show();

        if (comment == null) {
            ParseQuery<Route> query = ParseQuery.getQuery(Route.class);
            query.getInBackground(routeId, new GetCallback<Route>() {
                @Override
                public void done(Route object, ParseException e) {
                    if(e == null){
                        comment  = new Comment();
                        comment.setUser(ParseUser.getCurrentUser());
                        comment.setMessage(etComment.getText().toString());
                        comment.setRoute(object);

                        comment.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){
                                    progressDialog.dismiss();
                                    setResult(CommentFeedFragment.NEW_COMMENT, new Intent());
                                    finish();
                                } else{
                                }
                            }
                        });

                    } else{
                        Snackbar locSnackbar= Snackbar
                            .make(getCurrentFocus(), "Sorry an error occurred", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    postComment();
                                }
                            });
                        locSnackbar.show();
                    }
                }
            });
        }
    }


}

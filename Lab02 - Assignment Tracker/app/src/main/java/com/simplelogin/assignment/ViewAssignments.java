package com.simplelogin.assignment;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onkar.simplelogin.DBHelper;
import com.example.onkar.simplelogin.R;

import layout.exitFragment;

public class ViewAssignments extends AppCompatActivity implements exitFragment.OnFragmentInteractionListener  {
    private static DBHelper db;
    private static LinearLayout myLinearLayout;
    private static Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assignments);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        db = new DBHelper(this);
        myLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);


        Intent intent = getIntent();
        final String id = intent.getStringExtra("id"); //From login
        try {
            cursor = db.getAllAssignments(Integer.parseInt(id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int count = cursor.getCount();
        if (count == 0) {
            Toast.makeText(this,"Take a break! No assignments due.",Toast.LENGTH_LONG).show();
        } else {
            try {
                displayAll(cursor,count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void displayAll(Cursor c, int count)  throws Exception {
        final TextView[] myTextViews = new TextView[count]; // create an empty array;
        c.moveToFirst();
        for (int i = 0; i < count; i++) {
            // create a new textview
            TextView rowTextView = new TextView(this);
            getDetails(rowTextView, c);
            c.moveToNext();
            myLinearLayout.addView(rowTextView);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rowTextView.getLayoutParams();
            params.height = 200;
            rowTextView.setLayoutParams(params);
            // save a reference to the textview for later
            myTextViews[i] = rowTextView;
        }
    }

    private void getDetails(TextView tv, Cursor c) throws Exception {

        String text = "Topic: " + c.getString(0) + "\n" +
                "Course: " + c.getString(1) + "\n" +
                "Due Date: " + c.getString(2) + "\n" +
                "Notes: "+ c.getString(3) + ".";
        tv.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}

package com.simplelogin.assignment;


import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.onkar.simplelogin.DBHelper;
import com.example.onkar.simplelogin.R;

import layout.exitFragment;

public class Main2Activity extends AppCompatActivity implements exitFragment.OnFragmentInteractionListener {
    private static Button viewBtn, createBtn;
    private static DBHelper db;
    private static EditText topicEditText, subjectEditText, dueEditText, notesEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        viewBtn = (Button) findViewById(R.id.viewBtn);
        createBtn = (Button) findViewById(R.id.createBtn);
        topicEditText = (EditText) findViewById(R.id.topicTextView);
        subjectEditText = (EditText) findViewById(R.id.subjectTextView);
        dueEditText = (EditText) findViewById(R.id.dueTextView);
        notesEditText = (EditText) findViewById(R.id.notesTextView);
        db = new DBHelper(this);
        Intent intent = getIntent();
        String fName = intent.getStringExtra("userName");
        final String id = intent.getStringExtra("id"); //From login
//        String uid = intent.getStringExtra("_id"); // From Signup

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long returnVal = db.insertRecord(topicEditText.getText().toString(),dueEditText.getText().toString(),subjectEditText.getText().toString(),notesEditText.getText().toString(),Integer.parseInt(id));
                if (returnVal == -1 ) {
                    Toast.makeText(getApplicationContext(), "Oops, something went wrong! Please Try Again.", Toast.LENGTH_SHORT).show();
                } else
                Toast.makeText(getApplicationContext(), "Assignment Created Successfully!!", Toast.LENGTH_LONG).show();
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Main2Activity.this, ViewAssignments.class);
                i.putExtra("id",id);
                startActivityForResult(i, 500);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

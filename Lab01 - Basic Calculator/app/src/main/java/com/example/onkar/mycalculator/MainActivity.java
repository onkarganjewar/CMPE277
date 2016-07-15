package com.example.onkar.mycalculator;

import android.app.AlertDialog;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
/**
 * Created by
 * Onkar Ganjewar
 */

public class MainActivity extends AppCompatActivity {

    TextView totalTextView;
    EditText digit1_TextView;
    EditText digit2_TextView;
    Button calcButton, addBtn, subBtn, divBtn, mulBtn, percBtn, clrBtn;
    private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        digit1_TextView = (EditText) findViewById(R.id.digit1_textView);
        digit2_TextView = (EditText) findViewById(R.id.digit2_textView);
        calcButton = (Button) findViewById(R.id.calcButton);
        addBtn = (Button) findViewById(R.id.addBtn);
        subBtn = (Button) findViewById(R.id.subBtn);
        mulBtn = (Button) findViewById(R.id.mulBtn);
        divBtn = (Button) findViewById(R.id.divBtn);
        percBtn = (Button) findViewById(R.id.percBtn);
        clrBtn = (Button) findViewById(R.id.clrBtn);

        addBtn.setOnClickListener(onClickListener);
        subBtn.setOnClickListener(onClickListener);
        mulBtn.setOnClickListener(onClickListener);
        divBtn.setOnClickListener(onClickListener);
        percBtn.setOnClickListener(onClickListener);
        clrBtn.setOnClickListener(onClickListener);
        calcButton.setOnClickListener(onClickListener);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        int clicked = 0;

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.addBtn:
                    clicked = R.id.addBtn;
                    break;
                case R.id.subBtn:
                    clicked = R.id.subBtn;
                    break;
                case R.id.mulBtn:
                    clicked = R.id.mulBtn;
                    break;
                case R.id.divBtn:
                    clicked = R.id.divBtn;
                    break;
                case R.id.percBtn:
                    clicked = R.id.percBtn;
                    break;
                case R.id.clrBtn:
                    clicked = R.id.clrBtn;
                    digit1_TextView.setText("");
                    digit2_TextView.setText("");
                    break;
                case R.id.calcButton:
                    generateResult(clicked);
                    break;
                default:
                    throw new RuntimeException("Unknown button ID");
            }
        }
    };

    private void generateResult(int clicked) {

        double total = 0, num1 = 0, num2 = 0;

        try {
            num1 = Double.parseDouble(digit1_TextView.getText().toString());
            num2 = Double.parseDouble(digit2_TextView.getText().toString());
        } catch (java.lang.NumberFormatException ex) {
            // Display a message to the user about the wrong input
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Invalid Number");
            alertDialog.setMessage("Please Enter a Valid Number");
            alertDialog.show();
            ex.printStackTrace();
        }
        try {
            switch (clicked) {
                case R.id.addBtn:
                    total = num1 + num2;
                    totalTextView.setText(Double.toString(total));
                    break;
                case R.id.subBtn:
                    total = num1 - num2;
                    totalTextView.setText(Double.toString(total));
                    break;

                case R.id.mulBtn:
                    total = num1 * num2;
                    totalTextView.setText(Double.toString(total));
                    break;

                case R.id.divBtn:
                    total = num1 / num2;
                    Log.v("OUTPUT ", String.valueOf(total));
                    totalTextView.setText(String.valueOf(total));
                    break;
                case R.id.percBtn:
                    total = num1 % num2;
                    Log.v("OUTPUT ", String.valueOf(total));
                    totalTextView.setText(String.valueOf(total));
                    break;
                case R.id.clrBtn:
                    digit1_TextView.setText("");
                    digit2_TextView.setText("");
                    break;
                default:
                    break;
            }
        }catch (Exception e) {
            // Catch Exception
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.onkar.mycalculator/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.onkar.mycalculator/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
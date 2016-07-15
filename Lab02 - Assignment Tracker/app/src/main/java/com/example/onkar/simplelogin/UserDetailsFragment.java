package com.example.onkar.simplelogin;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Onkar on 6/25/2016.
 */
public class UserDetailsFragment extends Fragment {

    private static EditText password_editText, name_editText, phone_editText, email_editText;
    private static Context ctx;
    private static String name, password, phone, email;
    UserDetailsListener activityCommander;

    public UserDetailsFragment() {

    }
    public interface UserDetailsListener {
        public String searchUserSQL(String name, String password);
        public void registrationSQL(String name, String phone, String email, String password);
    }
/*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activityCommander = (UserDetailsListener) activity;

        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.ctx = context;
        try {
            activityCommander = (UserDetailsListener) context;
            Activity activity = context instanceof Activity ? (Activity) context : null;
        }catch (Exception e) {
            try {
                throw new Exception("Not able to instantiate fragment");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw new ClassCastException(context.toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.user_details_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        name_editText = (EditText) view.findViewById(R.id.name_EditText);
        email_editText = (EditText) view.findViewById(R.id.email_editText);
        phone_editText = (EditText) view.findViewById(R.id.phone_EditText);
        password_editText = (EditText) view.findViewById(R.id.passwordEditText);

        final Button registerBtn = (Button) view.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    buttonClicked(view);
            }
        });

    }

    private void findButtonClick(View view) {
        activityCommander.searchUserSQL(name, password);
    }

    private void buttonClicked(View v) {
        name = name_editText.getText().toString();
        password = password_editText.getText().toString();
        phone = phone_editText.getText().toString();
        email = email_editText.getText().toString();
        try {
            if (name.isEmpty()) {
                show("Please enter a valid name");
            } else if (password.isEmpty()) {
                show("Please enter a password");
            } else
            activityCommander.registrationSQL(name, phone, email, password);
        } catch (java.lang.NumberFormatException ex) {
            ex.printStackTrace();
            show("Please enter a valid number");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show(String s) {
            Toast.makeText(ctx, s, Toast.LENGTH_LONG).show();
    }
}


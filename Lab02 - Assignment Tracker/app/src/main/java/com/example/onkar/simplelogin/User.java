package com.example.onkar.simplelogin;

/**
 * Created by Onkar on 6/26/2016.
 */
public class User {
    // Labels table name
    public static final String TABLE_NAME = "User";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_name = "name";
    public static final String KEY_email = "email";
//    public static final String KEY_address = "address";
    public static final String KEY_password = "password";
    public static final String KEY_phone = "phone";

    // property help us to keep data
    public long phone;
    public String name;
    public String email;
    public int id;
    public String password;
}

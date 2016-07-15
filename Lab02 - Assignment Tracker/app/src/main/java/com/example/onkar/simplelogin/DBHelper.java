package com.example.onkar.simplelogin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;
import android.widget.Toast;

import com.simplelogin.assignment.AssignmentModel;

/**
 * Created by Onkar on 6/26/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 6;
    private static SQLiteDatabase db = null;
    // Database Name
    private static final String DATABASE_NAME = "AssignmentTracker.db";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_USERNAME = "UserName";
    private static final String COLUMN_PASSWORD = "Password";
    private static final String COLUMN_EMAIL = "Email Address";

    // Parent Table
    private static final
    String CREATE_TABLE_STUDENT= "CREATE TABLE " + User.TABLE_NAME  + "("
           + User.KEY_name + " TEXT, "
           + User.KEY_ID+ " integer primary key autoincrement, "
           + User.KEY_password + " TEXT, "
           + User.KEY_phone + " LONG, "
           + User.KEY_email + " TEXT )";

    // Child Table
    private static final
    String CREATE_TABLE_ASSIGNMENTS = "CREATE TABLE IF NOT EXISTS "+ AssignmentModel.TABLE_NAME + " ("
           + AssignmentModel.COLUMN_TITLE + " TEXT NOT NULL, "
           + AssignmentModel.COLUMN_COURSE + " TEXT, "
           + AssignmentModel.COLUMN_DUEDATE + " DATE, "
           + AssignmentModel.COLUMN_NOTES + " VARCHAR, "
           + AssignmentModel.COLUMN_ID + " INTEGER, "
           + "FOREIGN KEY("+AssignmentModel.COLUMN_ID+") REFERENCES "
           + User.TABLE_NAME + "(" +User.KEY_ID+ ")" +
           ")";

    public DBHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_ASSIGNMENTS);
        boolean value = db.isDatabaseIntegrityOk();
        System.out.println(value);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_NAME);
        onCreate(db);
    }
    public long addUser(User user) {
        //Open connection to write data
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(User.KEY_phone, user.phone);
        values.put(User.KEY_password,user.password);
        values.put(User.KEY_email, user.email);
        values.put(User.KEY_name, user.name);
        long id = db.insert(User.TABLE_NAME, null, values);
        db.close(); // Closing database connection
        return id;
    }

    public String findUserSQL(String username, String password) {
        String query = "Select "+User.KEY_ID+" FROM " + User.TABLE_NAME + " WHERE " + User.KEY_name + " =  \"" + username + "\" AND "+User.KEY_password + " =  \"" + password + "\"";
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor.moveToFirst()) {
            String id = cursor.getString( cursor.getColumnIndex(User.KEY_ID) ); // id is column name in db
            return id;
        }
        return "null";
    }

    public boolean findUser(String username, String password) {
        String query = "Select * FROM " + User.TABLE_NAME + " WHERE " + User.KEY_name + " =  \"" + username + "\" AND "+User.KEY_password + " =  \"" + password + "\"";
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        boolean val = cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            return true;
        } else
            return false;
    }

    //---insert a record into the database---
    public long insertRecord(String title, String duedate, String course, String notes, int id) {

        ContentValues initialValues = new ContentValues();
        	initialValues.put(AssignmentModel.COLUMN_TITLE, title);
        	initialValues.put(AssignmentModel.COLUMN_DUEDATE, duedate);
        	initialValues.put(AssignmentModel.COLUMN_COURSE, course);
        	initialValues.put(AssignmentModel.COLUMN_NOTES, notes);
            initialValues.put(AssignmentModel.COLUMN_ID, id);
            long returnId = db.insert(AssignmentModel.TABLE_NAME, null, initialValues);
        	return returnId;
    }

    //---get all Records--
    public Cursor getAllAssignments(int rowId) throws Exception {
        String query = "SELECT * from "+AssignmentModel.TABLE_NAME+" where "+AssignmentModel.COLUMN_ID+" = "+rowId+" ;";
        Cursor c = db.rawQuery(query,null);
        return c;
    }

}

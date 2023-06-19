package com.example.haddany.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.haddany.model.Activity;

import java.util.ArrayList;
import java.util.List;

public class MyDataBase extends SQLiteOpenHelper {
    private Context context;
    private static String DATABASE_NAME = "database";
    private static int DATABASE_VERSION = 1;

    private static String TABLE_ACTIVITY = "activity";
    private static String COLUMN_USER = "user";
    private static String COLUMN_ACTIVITY = "type";
    private static String COLUMN_DATE = "date";
    private static String COLUMN_LATITUDE = "latitude";
    private static String COLUMN_LONGITUDE = "longitude";
    private static String COLUMN_SPEED = "speed";


    public MyDataBase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String script = "CREATE TABLE " + TABLE_ACTIVITY + "("
                + COLUMN_USER + " TEXT," + COLUMN_ACTIVITY + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LATITUDE + " TEXT," + COLUMN_LONGITUDE + " TEXT,"
                + COLUMN_SPEED + " TEXT" + ")";
        // Execute script.
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY);
        // Recreate
        onCreate(db);
    }

    // Ajouter un user
    public Long addActivity(Activity activity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER, activity.getUser());
        cv.put(COLUMN_ACTIVITY, activity.getTypeActivity());
        cv.put(COLUMN_DATE, activity.getDate());
        cv.put(COLUMN_LONGITUDE, activity.getLatitude());
        cv.put(COLUMN_LATITUDE, activity.getLongitude());
        cv.put(COLUMN_SPEED, activity.getSpeed());
        Long res = db.insert(TABLE_ACTIVITY, null, cv);

        return res;
    }
    public List<Activity> getActivities(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ACTIVITY, null);
        List<Activity> activities = new ArrayList<>();
        int index;
        while (c.moveToNext()){
            index = c.getColumnIndexOrThrow("user");
            String user = c.getString(index);
            index = c.getColumnIndexOrThrow("type");
            String typeActivity = c.getString(index);
            index = c.getColumnIndexOrThrow("date");
            String date = c.getString(index);
            index = c.getColumnIndexOrThrow("latitude");
            double latitude = Double.parseDouble(c.getString(index));
            index = c.getColumnIndexOrThrow("longitude");
            double longitude = Double.parseDouble(c.getString(index));
            index = c.getColumnIndexOrThrow("speed");
            double speed = Double.parseDouble(c.getString(index));

            Activity activity = new Activity(user, typeActivity, date, latitude, longitude, speed);
            activities.add(activity);
        }
        return activities;
    }
}

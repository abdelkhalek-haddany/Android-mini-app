package com.example.haddany.model;

import java.util.Date;

public class Activity {
    private String user;
    private String typeActivity;
    private String date;
    private double latitude;
    private double longitude;
    private double speed;

    public Activity(String user, String typeActivity, String date, double latitude, double longitude, double speed) {
        this.user = user;
        this.typeActivity = typeActivity;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public Activity(String user, String typeActivity, double latitude, double longitude, double speed) {
        this.user = user;
        this.typeActivity = typeActivity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.date = new Date().toString();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTypeActivity() {
        return typeActivity;
    }

    public void setTypeActivity(String typeActivity) {
        this.typeActivity = typeActivity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

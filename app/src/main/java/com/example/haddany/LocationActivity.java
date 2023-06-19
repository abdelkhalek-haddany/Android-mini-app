package com.example.haddany;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.haddany.db.MyDataBase;
import com.example.haddany.model.Activity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationActivity extends AppCompatActivity implements SensorEventListener {


    //String email = getIntent().getStringExtra("email");

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    double longitude, latitude, speed;
    private TextView action;
    private String email;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private ImageView activityIcon;
    MyDataBase mydb = new MyDataBase(LocationActivity.this);

    double magnitudeP = 0, mPrevious = 0;
    double magnitudeD, mDelta;
    float x, y, z;
    int old_count = 0;
    String uri = "@drawable-hdpi";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        email = "elidrissi1818@gmail.com";
        /**************** Activity *****/
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        action = (TextView) findViewById(R.id.idTextActivity);

        //activityIcon = findViewById(R.id.activityIcon);

        /**************** Get location in maps *****/
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(LocationActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(LocationActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //clicked menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.action_profile:
                startActivity(new Intent(LocationActivity.this, EditProfileActivity.class));
                return true;
            case R.id.action_map:
                startActivity(new Intent(LocationActivity.this, LocationActivity.class));
                return true;
            case R.id.action_activities:
                startActivity(new Intent(LocationActivity.this, ActionActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Get current location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            MarkerOptions options = new MarkerOptions().position(latLng).title("I am there");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            googleMap.addMarker(options);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    //Get actions
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        x = values[0];
        y = values[1];
        z = values[2];

        //
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
            last_x = x;
            last_y = y;
            last_z = z;
        }
        int count = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // activité : Sauter
        double d = Math.round(Math.sqrt(Math.pow(2, x) + Math.pow(2, y) + Math.pow(2, z)) - 2);
        float threshold_sauter = preferences.getFloat("sauter", 10);
        // activité : Marcher
        double Magnitude = Math.sqrt(Math.pow(2, x) + Math.pow(2, y) + Math.pow(2, z));
        magnitudeD = Magnitude - magnitudeP;
        magnitudeP = Magnitude;
        float threshold_marcher = preferences.getFloat("marcher", 5);
        // activité : Assis
        double m = Math.sqrt(Math.pow(2, x) + Math.pow(2, y) + Math.pow(2, z));
        mDelta = m - mPrevious;
        mPrevious = m;
        float threshold_assis = preferences.getFloat("assis", 1);

        if (d != 0 && d <= threshold_sauter) {
            count = 1;
        } else if (magnitudeD > threshold_marcher) {
            count = 2;
        } else if (mDelta > threshold_assis) {
            count = 3;
        }
        String detail;
        if (count == 1) {
            detail = "Latitude : " + latitude + "\nLongitude : "
                    + longitude + "\nSpeed [m/s] : " + speed;
            action.setText("Activity : Jumped\n" + detail);
//            mydb.addActivity(new Activity(email, "Jumped", latitude, longitude, speed));
            if (count != old_count) {
                mydb.addActivity(new Activity(email, "Jumped", latitude, longitude, speed));
                old_count = count;
            }
            action.invalidate();
        } else if (count == 2) {
            detail = "Latitude : " + latitude + "\nLongitude : "
                    + longitude + "\nSpeed [m/s] : " + speed;
            // action.setText("Activity : Running\n"+detail);
            if (count != old_count) {
                mydb.addActivity(new Activity(email, "Running", latitude, longitude, speed));
                old_count = count;
            }
            // action.invalidate();
            System.out.printf("Running");
        } else if (count == 3) {
            detail = "Latitude : " + latitude + "\nLongitude : "
                    + longitude + "\nSpeed [m/s] : " + 0;
            if (count != old_count) {
                mydb.addActivity(new Activity(email, "Sitting", latitude, longitude, 0));
                old_count = count;
            }
            action.setText("Activity : Sitting\n" + detail);
            action.invalidate();
        } else if (count == 0 && z < 4) {
            detail = "Latitude : " + latitude + "\nLongitude : "
                    + longitude + "\nSpeed [m/s] : " + speed;
            if (count != old_count) {
                mydb.addActivity(new Activity(email, "Sitting", latitude, longitude, speed));
                old_count = count;
            }
            action.setText("Activity : Standing\n" + detail);
            action.invalidate();
        }

    }
}

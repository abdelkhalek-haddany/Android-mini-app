package com.example.haddany;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
            case R.id.action_profile:
                startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                return true;
            case R.id.action_map:
                startActivity(new Intent(HomeActivity.this, LocationActivity.class));
                return true;
            case R.id.action_activities:
                startActivity(new Intent(HomeActivity.this, ActionActivity.class));
                return true;
        }        return super.onOptionsItemSelected(item);
    }

    public void profile(View view) {
        Intent i = new Intent(this, EditProfileActivity.class);
        startActivity(i);
    }

    public void map(View view) {
        Intent i = new Intent(this,LocationActivity.class);
        startActivity(i);
    }

    public void activities(View view) {
        Intent i = new Intent(this,ActionActivity.class);
        startActivity(i);
    }

    public void logout(View view) {
        SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putStringSet("email",null );
        Ed.putStringSet("password", null);
        Ed.commit();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
}
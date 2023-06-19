package com.example.haddany;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haddany.adapter.AdapterActivity;
import com.example.haddany.db.MyDataBase;
import com.example.haddany.model.Activity;

import java.util.Collections;
import java.util.List;

public class ActionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    MyDataBase mydb = new MyDataBase(ActionActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        recyclerView = findViewById(R.id.idRecHistory);
        List<Activity> activities = mydb.getActivities();
        Collections.reverse(activities);
        AdapterActivity adapter = new AdapterActivity(this, activities);
        recyclerView.setAdapter(adapter);
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
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
        case R.id.action_profile:
            startActivity(new Intent(ActionActivity.this, EditProfileActivity.class));
            return true;
        case R.id.action_map:
            startActivity(new Intent(ActionActivity.this, LocationActivity.class));
            return true;
        case R.id.action_activities:
            startActivity(new Intent(ActionActivity.this, ActionActivity.class));
            return true;
    }        return super.onOptionsItemSelected(item);
    }
}
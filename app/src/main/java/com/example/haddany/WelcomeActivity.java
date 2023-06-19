package com.example.haddany;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class WelcomeActivity extends AppCompatActivity {
    ImageView register,login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        register.setOnClickListener(e->{
            Intent intent = new Intent(this, RegisterActivityu.class);
            startActivity(intent);
        });

        login.setOnClickListener(e->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }
}
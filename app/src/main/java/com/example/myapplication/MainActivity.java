package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    public static int clicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Home");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }


    public void Play(View v){
        clicked = 0;
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        startActivity(intent);
    }

    public void Exit(View v){
        clicked++;
        Toast.makeText(this, "Press exit again to close the game!", Toast.LENGTH_SHORT).show();
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                clicked = 0;
            }
        };
        timer.schedule(task, 3000);

        if(clicked > 1){
            clicked = 0;
            finish();
            finishAffinity();
        }
    }

    @Override
    public void onBackPressed(){
        Exit(null);
    }
}
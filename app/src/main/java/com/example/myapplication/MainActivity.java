package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static int clicked = 0;
    private ConstraintLayout mainLayout;
    private boolean menuVisible = false;

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

        mainLayout = findViewById(R.id.main);

        // Initialize the SoundManager
        SoundManager.getInstance(this);

        // Add entrance animation
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        findViewById(R.id.textView).startAnimation(slideIn);

        // Add staggered animation for buttons
        animateButton(findViewById(R.id.playButton), 300);
        animateButton(findViewById(R.id.statsButton), 400);
        animateButton(findViewById(R.id.settingsButton), 500);
        animateButton(findViewById(R.id.exitButton), 600);
    }

    private void animateButton(View button, int delay) {
        button.setAlpha(0f);
        button.setTranslationY(100f);
        button.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(500)
                .start();
    }

    public void Play(View v){
        clicked = 0;

        // Play button click sound
        SoundManager.getInstance(this).playButtonClick();

        Intent intent = new Intent(this, ModeSelectionActivity.class);
        startActivity(intent);
    }

    public void openStats(View v) {
        animateButtonPress(v);

        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void openSettings(View v) {
        animateButtonPress(v);

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void animateButtonPress(View button) {
        ScaleAnimation scale = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(100);
        scale.setRepeatCount(1);
        scale.setRepeatMode(Animation.REVERSE);
        button.startAnimation(scale);
    }

    public void Exit(View v){
        clicked++;

        // Play button click sound
        SoundManager.getInstance(this).playButtonClick();


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

            // Add exit animation
            mainLayout.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        finish();
                        finishAffinity();
                    })
                    .start();
        }
    }

    @Override
    public void onBackPressed(){
        Exit(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).release();
    }


}
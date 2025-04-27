package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.textView5);
        textView.setShadowLayer(1.5f, 0, 0, Color.BLACK);

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        ((TextView)findViewById(R.id.textView5)).startAnimation(anim);

        Intent intent = getIntent();
        Player player = (Player) intent.getSerializableExtra("player");
        if(!player.isPlayerPlayingRussianRoulette){
            new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    Intent intent = new Intent(LoadingScreenActivity.this, NormalMode.class);
                    intent.putExtra("player", player);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            },1000);
        }else{
            new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    Intent intent = new Intent(LoadingScreenActivity.this, RussianModeLoadingScreen.class);
                    intent.putExtra("player", player);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            },1000);
        }

    }
}
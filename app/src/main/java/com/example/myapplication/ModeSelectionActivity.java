package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ModeSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mode_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void exit(View v){
        finish();
    }

    public void normalMode(View view) {
        Intent intent = new Intent(this, LoadingScreenActivity.class);
        intent.putExtra("mode", "normal");
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = false;
        intent.putExtra("player", player);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    public void russianMode(View view) {
        Intent intent = new Intent(this, RussianModeLoadingScreen.class);
//        intent.putExtra("mode", "russian");
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = true;
        intent.putExtra("player", player);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
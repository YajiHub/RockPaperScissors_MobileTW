package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LiveFireGun extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_live_fire_gun);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Player player = (Player) getIntent().getSerializableExtra("player");
        boolean didPlayerWin = getIntent().getBooleanExtra("didWin", false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                if(didPlayerWin){
                    ((TextView) findViewById(R.id.conclusion)).setText(R.string.enemyDies);
                    ((TextView) findViewById(R.id.conclusion)).setVisibility(View.VISIBLE);

                }else{
                    ((TextView) findViewById(R.id.conclusion)).setText(R.string.playerDies);
                    ((TextView) findViewById(R.id.conclusion)).setVisibility(View.VISIBLE);
                }
                ((Button)findViewById(R.id.tryAgain)).setVisibility(Button.VISIBLE);
                ((Button)findViewById(R.id.playNormal)).setVisibility(Button.VISIBLE);
                ((Button)findViewById(R.id.exit)).setVisibility(Button.VISIBLE);
            }
        },1300);


    }


    public void exit(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void tryAgain(View view){
        Intent intent = new Intent(this, RussianModeLoadingScreen.class);
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = true;
        intent.putExtra("player", player);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    public void playNormal(View view){
        Intent intent = new Intent(this, LoadingScreenActivity.class);
        intent.putExtra("mode", "normal");
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = false;
        intent.putExtra("player", player);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }










}
package com.example.myapplication;

import android.content.Intent;
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

public class blankFireGun extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blank_fire_gun);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        ((TextView)findViewById(R.id.continueRound)).startAnimation(anim);

        // Play empty gun sound
        SoundManager.getInstance(this).playGunEmpty();

        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent intent = new Intent(blankFireGun.this, NormalMode.class);
                Player player = (Player) getIntent().getSerializableExtra("player");
                intent.putExtra("player", player);
                Russian_Roulette russian_roulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");
                intent.putExtra("russian_roulette", russian_roulette);
                Computer_Player computer = (Computer_Player) getIntent().getSerializableExtra("computer");
                intent.putExtra("computer", computer);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        },1000);
    }
}
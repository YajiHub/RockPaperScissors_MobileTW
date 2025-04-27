package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class showChosenCards extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_chosen_cards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Player player = (Player) getIntent().getSerializableExtra("player");
        Computer_Player computer = (Computer_Player) getIntent().getSerializableExtra("computer");



        String number = Integer.toString(player.getHand1().getHandNumber());
        String number2 = Integer.toString(player.getHand2().getHandNumber());
        String computer1 = Integer.toString(computer.getHand1().getHandNumber());
        String computer2 = Integer.toString(computer.getHand2().getHandNumber());

//        String hands = number + "," + number2 + "Enemy: " + computer1 + "," + computer2;
//        ((TextView)findViewById(R.id.debug)).setText(hands);

        //player chosen cards
        switch(player.getHand1().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.chosenText1)).setText("Rock");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.rock);
                break;
            case 2:
                ((TextView)findViewById(R.id.chosenText1)).setText("Paper");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.paper);
                break;
            case 3:
                ((TextView)findViewById(R.id.chosenText1)).setText("Scissor");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.scissor);
                break;
        }
        switch(player.getHand2().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.chosenText2)).setText("Rock");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.rock);
                break;
            case 2:
                ((TextView)findViewById(R.id.chosenText2)).setText("Paper");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.paper);
                break;
            case 3:
                ((TextView)findViewById(R.id.chosenText2)).setText("Scissor");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.scissor);
                break;
        }

        //enemy chosen cards
        switch(computer.getHand1().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.playerFinal)).setText("Rock");
                ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.rock);
                break;
            case 2:
                ((TextView)findViewById(R.id.playerFinal)).setText("Paper");
                ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.paper);
                break;
            case 3:
                ((TextView)findViewById(R.id.playerFinal)).setText("Scissor");
                ((ImageView)findViewById(R.id.finalPlayerCard)).setImageResource(R.drawable.scissor);
                break;
        }
        switch(computer.getHand2().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.enemy2)).setText("Rock");
                ((ImageView)findViewById(R.id.enemyChosen2)).setImageResource(R.drawable.rock);
                break;
            case 2:
                ((TextView)findViewById(R.id.enemy2)).setText("Paper");
                ((ImageView)findViewById(R.id.enemyChosen2)).setImageResource(R.drawable.paper);
                break;
            case 3:
                ((TextView)findViewById(R.id.enemy2)).setText("Scissor");
                ((ImageView)findViewById(R.id.enemyChosen2)).setImageResource(R.drawable.scissor);
                break;
        }


        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent intent = new Intent(showChosenCards.this, FinalHandPicking.class);
                intent.putExtra("player", player);
                intent.putExtra("computer", computer);
                intent.putExtra("russian_roulette", (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette"));
                intent.putExtra("roundText", getIntent().getStringExtra("roundText"));
                intent.putExtra("probabilityOfDyingText", getIntent().getStringExtra("probabilityOfDyingText"));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        },1000);

    }
}
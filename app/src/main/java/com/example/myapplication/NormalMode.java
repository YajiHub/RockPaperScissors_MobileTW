package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NormalMode extends AppCompatActivity {
    CustomCountdownTimer customTimer;
//    static ArrayList<Integer> hands = new ArrayList<>();

    Player player;
    Computer_Player computer = new Computer_Player();
    Russian_Roulette russian_roulette;

    //only used for russian roulette mode
    static String roundText;
    static String probabilityOfDyingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_normal_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        player = (Player) getIntent().getSerializableExtra("player");
        player.hand1 = null;
        player.hand2 = null;
        player.finalHand = null;
        russian_roulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");

        ((TextView)findViewById(R.id.paperChosen)).setText("");
        ((TextView)findViewById(R.id.rockChosen)).setText("");
        ((TextView)findViewById(R.id.scissorChosen)).setText("");

        if(player.isPlayerPlayingRussianRoulette){
            computer.isPlayerPlayingRussianRoulette = true;
            TextView rounds = ((TextView)findViewById(R.id.round));
            TextView probability = ((TextView)findViewById(R.id.probability));

            roundText = "Round: " + (russian_roulette.getCurrentRound()+1) + "/" + russian_roulette.getTotalRounds();
            Double percent = (double) (((double) 1 / (russian_roulette.getTotalRounds() - russian_roulette.getCurrentRound())) * 100);
            probabilityOfDyingText = probability.getText().toString() + String.format(" %.2f", percent) + "%";

            rounds.setText(roundText);
            probability.setText(probabilityOfDyingText);
            rounds.setVisibility(View.VISIBLE);
            probability.setVisibility(View.VISIBLE);

        }else{
            computer.isPlayerPlayingRussianRoulette = false;
        }


        TextView timerText = findViewById(R.id.timerText);
        ProgressBar countdown = findViewById(R.id.countdown);

        customTimer = new CustomCountdownTimer(timerText, countdown, 7, 100, new CustomCountdownTimer.OnTimerFinishListener() {
            @Override
            public void onTimerFinish() {
                // Timer end logic
                player.generateHands();
//                hands.add(player.getHand1().getHandNumber());
//                hands.add(player.getHand2().getHandNumber());
                throwHands(null);
            }
        });

        customTimer.start();
//        customTimer.cancel();

    }



    /*
    Current game is limited to choosing different hands, players are not allowed to have both of their hands
    carry the same card (hand1: Rock, hand2: Rock -> is not allowed)
     */
    public void isChosen(View cardButtons){

        /*
        Tag:
        1 - Rock
        2 - Paper
        3 - Scissor
        functions to set cards to hand1 and hand2

        at start that card is put to hand1 and then if another different card is chosen put to hand 2
        but if hand1 has rock and user taps rock again then both hand1 and hand2 should contain rock and now both hands are occupied

        if both hands are occupied then if both hands have same hand if the user picks that same hand again
        then it erases the duplication meaning the user chose to unselect that hand

        if both hands are occupied but both hands have different hand then if hand1 has rock and hand2 has paper
        if user taps rock again then the rock would be unselected thus the user would then has paper as selected hand
        but if user picks different card like scissor then that hand1 which is the first chosen would be replaced with scissor like first in first our

         */


//
//
//        if(player.getHand1() == null || player.getHand2() == null){
//            if(player.getHand1() == null){
//                player.setHand1(tag);
//            }else{
//                player.setHand2(tag);
//            }
//        }else{
//            if(player.getHand1().getHandNumber() == tag && player.getHand2().getHandNumber() == tag){
//                if(player.getHand1().getHandNumber() != tag){
//                    player.setHand1(tag);
//                }else{
//                    player.hand2 = null;
//                }
//            }else{
//                if(player.getHand1().getHandNumber() != tag || player.getHand2().getHandNumber() != tag){
//                    player.setHand1(tag);
//                }else{
//                    if(player.getHand1().getHandNumber() == tag){
//                        player.setHand1(player.getHand2().getHandNumber());
//                        player.hand2 = null;
//                    }else{
//                        player.hand2 = null;
//                    }
//                }
//            }
//        }

        int tag = Integer.parseInt(cardButtons.getTag().toString());

        if (player.getHand1() != null && player.getHand1().getHandNumber() == tag) {
            if (player.hand2 != null) {
                player.setHand1(player.getHand2().getHandNumber());
                player.hand2 = null;
            } else {
                player.hand1 = null;
            }
        } else if (player.hand2 != null && player.getHand2().getHandNumber() == tag) {
            player.hand2 = null;
        } else {
            if (player.getHand1() == null) {
                player.setHand1(tag);
            } else if (player.hand2 == null) {
                player.setHand2(tag);
            } else {
                player.setHand1(player.getHand2().getHandNumber());
                player.setHand2(tag);
            }
        }

        // reloading the hints after any changes
        updateCardHints();

    }

    private void updateCardHints() {
        TextView rockHint = findViewById(R.id.rockChosen);
        TextView paperHint = findViewById(R.id.paperChosen);
        TextView scissorHint = findViewById(R.id.scissorChosen);

        // Reset all hints
        rockHint.setText("");
        paperHint.setText("");
        scissorHint.setText("");

        // Add "⭐" for selected hands
        if (player.getHand1() != null) {
            switch (player.getHand1().getHandNumber()) {
                case 1: rockHint.setText("⭐"); break;
                case 2: paperHint.setText("⭐"); break;
                case 3: scissorHint.setText("⭐"); break;
            }
        }
        if (player.getHand2() != null) {
            switch (player.getHand2().getHandNumber()) {
                case 1: rockHint.setText("⭐"); break;
                case 2: paperHint.setText("⭐"); break;
                case 3: scissorHint.setText("⭐"); break;
            }
        }
    }


    public void forfeitGame(View forfeitButton) {
        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }
//        hands.clear();
        finish();
    }

    public void throwHands(View throwButton){
        if(player.getHand1() == null || player.getHand2() == null){
            Toast.makeText(this, "Please choose two hands", Toast.LENGTH_SHORT).show();
        }else{
            // Cancel the timer
            if (customTimer != null) {
                customTimer.cancel();
                customTimer = null;
            }


//            player.setPlayerHands(hands.get(0), hands.get(1));
            Intent intent = new Intent(this, showChosenCards.class);
            intent.putExtra("player", player);
            computer.generateHands();
            intent.putExtra("computer", computer);
            if(player.isPlayerPlayingRussianRoulette){
                intent.putExtra("russian_roulette", russian_roulette);
                intent.putExtra("roundText", roundText);
                intent.putExtra("probabilityOfDyingText", probabilityOfDyingText);
            }
            startActivity(intent);
            finish();
//            hands.clear();
        }

    }

    @Override
    public void onBackPressed() {
        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }
        super.onBackPressed();
    }

}
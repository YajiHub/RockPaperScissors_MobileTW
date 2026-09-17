package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FinalHandPicking extends AppCompatActivity {
    private SoundManager soundManager;

    CustomCountdownTimer customTimer;

    private Player playerUser;
    private Computer_Player computerUser;

    private int chosenHand = -1;

    private String chosenSymbol = "⭐";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_hand_picking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize sound manager
        soundManager = SoundManager.getInstance(this);

        Player player = (Player) getIntent().getSerializableExtra("player");
        Computer_Player computer = (Computer_Player) getIntent().getSerializableExtra("computer");
        String roundText = getIntent().getStringExtra("roundText");
        String probabilityOfDyingText = getIntent().getStringExtra("probabilityOfDyingText");

        if(player.isPlayerPlayingRussianRoulette){
            ((TextView)findViewById(R.id.probability)).setText(probabilityOfDyingText);
            ((TextView)findViewById(R.id.probability)).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.round)).setText(roundText);
            ((TextView)findViewById(R.id.round)).setVisibility(View.VISIBLE);
        }

        playerUser = player;
        computerUser = computer;

        ((TextView)findViewById(R.id.finalCard1)).setText("");
        ((TextView)findViewById(R.id.finalCard2)).setText("");


        //player chosen cards
        switch(player.getHand1().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.chosenText1)).setText("Rock");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.rock);
                ((ImageButton)findViewById(R.id.cardButton1)).setTag(1);
                ((TextView)findViewById(R.id.finalCard1)).setTag(1);
                break;
            case 2:
                ((TextView)findViewById(R.id.chosenText1)).setText("Paper");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.paper);
                ((ImageButton)findViewById(R.id.cardButton1)).setTag(2);
                ((TextView)findViewById(R.id.finalCard1)).setTag(2);
                break;
            case 3:
                ((TextView)findViewById(R.id.chosenText1)).setText("Scissor");
                ((ImageView)findViewById(R.id.chosenImg1)).setImageResource(R.drawable.scissor);
                ((ImageButton)findViewById(R.id.cardButton1)).setTag(3);
                ((TextView)findViewById(R.id.finalCard1)).setTag(3);
                break;
        }
        switch(player.getHand2().getHandNumber()){
            case 1:
                ((TextView)findViewById(R.id.chosenText2)).setText("Rock");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.rock);
                ((ImageButton)findViewById(R.id.cardButton2)).setTag(1);
                ((TextView)findViewById(R.id.finalCard2)).setTag(1);
                break;
            case 2:
                ((TextView)findViewById(R.id.chosenText2)).setText("Paper");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.paper);
                ((ImageButton)findViewById(R.id.cardButton2)).setTag(2);
                ((TextView)findViewById(R.id.finalCard2)).setTag(2);
                break;
            case 3:
                ((TextView)findViewById(R.id.chosenText2)).setText("Scissor");
                ((ImageView)findViewById(R.id.chosenImg2)).setImageResource(R.drawable.scissor);
                ((ImageButton)findViewById(R.id.cardButton2)).setTag(3);
                ((TextView)findViewById(R.id.finalCard2)).setTag(3);
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


        TextView timerText = findViewById(R.id.timerText);
        ProgressBar countdown = findViewById(R.id.countdown);

        // Get timer duration from settings with safe fallback
        int timerDuration = getTimerDurationFromSettings();

        customTimer = new CustomCountdownTimer(timerText, countdown, timerDuration, 100, new CustomCountdownTimer.OnTimerFinishListener() {
            @Override
            public void onTimerFinish() {
                // Your end-of-timer logic
                playerUser.pickFinalHand(playerUser.getHand1(), playerUser.getHand2());
                chosenHand = playerUser.getFinalHand().getHandNumber();
                throwHand(null);
            }
        });

        customTimer.start();


    }

    /**
     * Safely get timer duration from settings
     * Returns default value (7) if settings can't be read
     */
    private int getTimerDurationFromSettings() {
        try {
            SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);

            // Check if timer is enabled (default true)
            boolean timerEnabled = prefs.getBoolean("timer_enabled", true);

            if (timerEnabled) {
                // Get timer progress (0-7) and convert to seconds (3-10)
                int timerProgress = prefs.getInt("timer_duration", 4); // default is 4 (7 seconds)
                return 3 + timerProgress; // Convert to actual seconds
            } else {
                // Timer disabled in settings, hide it
                View timerContainer = findViewById(R.id.timerContainer);
                if (timerContainer != null) {
                    timerContainer.setVisibility(View.GONE);
                }
                return 7; // Still need a value for the timer, even if hidden
            }
        } catch (Exception e) {
            // If anything goes wrong, use default value
            return 7;
        }
    }

    public void finalChoose(View cardButton) {
        // Play sound effect for card selection
        soundManager.playCardSelect();


        int tag = -1;
        if (cardButton != null && cardButton.getTag() != null) {
            try {
                tag = Integer.parseInt(cardButton.getTag().toString());
            } catch (Exception ignored) {}
        }
        if (tag == -1) return;

        int textView1Tag = -1;
        int textView2Tag = -1;
        View viewCard1 = findViewById(R.id.finalCard1);
        View viewCard2 = findViewById(R.id.finalCard2);
        if (viewCard1 != null && viewCard1.getTag() != null) {
            try { textView1Tag = Integer.parseInt(viewCard1.getTag().toString()); } catch (Exception ignored) {}
        }
        if (viewCard2 != null && viewCard2.getTag() != null) {
            try { textView2Tag = Integer.parseInt(viewCard2.getTag().toString()); } catch (Exception ignored) {}
        }

        //if there is no hand picked
        if(chosenHand == -1 ){
            chosenHand = tag;
            if(tag == textView1Tag){
                ((TextView)findViewById(R.id.finalCard1)).setText(chosenSymbol);
            }
            if(tag == textView2Tag){
                ((TextView)findViewById(R.id.finalCard2)).setText(chosenSymbol);
            }
            playerUser.setFinalHand(tag);
        }
        //if there is currently picked hand then switch to another
        else{
            if(tag!=chosenHand){
                if(chosenHand == textView1Tag){
                    ((TextView)findViewById(R.id.finalCard1)).setText("");
                    ((TextView)findViewById(R.id.finalCard2)).setText(chosenSymbol);
                }
                if(chosenHand == textView2Tag){
                    ((TextView)findViewById(R.id.finalCard2)).setText("");
                    ((TextView)findViewById(R.id.finalCard1)).setText(chosenSymbol);
                }
                chosenHand = tag;
                playerUser.setFinalHand(tag);
            } else {
                chosenHand = -1;
                ((TextView)findViewById(R.id.finalCard1)).setText("");
                ((TextView)findViewById(R.id.finalCard2)).setText("");
            }
        }

        // Toggle glowing selection state on the cards
        View cardBtn1 = findViewById(R.id.cardButton1);
        View cardBtn2 = findViewById(R.id.cardButton2);
        if (cardBtn1 != null) cardBtn1.setSelected(chosenHand == textView1Tag && chosenHand != -1);
        if (cardBtn2 != null) cardBtn2.setSelected(chosenHand == textView2Tag && chosenHand != -1);
        //closing part of final choose method
    }


    public void forfeitGame(View forfeitButton) {
        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }
        finish();
    }

    public void throwHand(View throwButton){

        // Cancel the timer
        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }

        if(chosenHand != -1){
            Intent intent = new Intent(this, showFinalHands.class);
            computerUser.pickFinalHand(playerUser.getHand1(), playerUser.getHand2());
            intent.putExtra("player", playerUser);
            intent.putExtra("computer", computerUser);
            if(playerUser.isPlayerPlayingRussianRoulette){
                Russian_Roulette russian_roulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");
                intent.putExtra("russian_roulette", russian_roulette);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.bounce_in, R.anim.pulse);
            chosenHand = -1;
            finish();
            playerUser = null;
            computerUser = null;
        }else{
            Toast.makeText(this, "Pick a hand!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.stopAllSounds();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }
    }
}
package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NormalMode extends AppCompatActivity {
    CustomCountdownTimer customTimer;
    Player player;
    Computer_Player computer = new Computer_Player();
    Russian_Roulette russian_roulette;
    private SoundManager soundManager;
    private Vibrator vibrator;

    //only used for russian roulette mode
    private String roundText;
    private String probabilityOfDyingText;

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

        // Get system vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize sound manager
        soundManager = SoundManager.getInstance(this);

        player = (Player) getIntent().getSerializableExtra("player");
        if (player == null) {
            player = new Player();
        }
        player.hand1 = null;
        player.hand2 = null;
        player.finalHand = null;
        russian_roulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");

        ((TextView)findViewById(R.id.paperChosen)).setText("");
        ((TextView)findViewById(R.id.rockChosen)).setText("");
        ((TextView)findViewById(R.id.scissorChosen)).setText("");

        if(player.isPlayerPlayingRussianRoulette){
            if (russian_roulette == null) {
                russian_roulette = new Russian_Roulette();
                russian_roulette.gunInitialize();
            }
            computer.isPlayerPlayingRussianRoulette = true;
            TextView rounds = ((TextView)findViewById(R.id.round));
            TextView probability = ((TextView)findViewById(R.id.probability));

            int remainingRounds = Math.max(1, russian_roulette.getTotalRounds() - russian_roulette.getCurrentRound());
            roundText = "Round: " + (russian_roulette.getCurrentRound()+1) + "/" + russian_roulette.getTotalRounds();
            Double percent = (double) (((double) 1 / remainingRounds) * 100);
            probabilityOfDyingText = "Lethal Odds: " + String.format("%.1f", percent) + "%";

            rounds.setText(roundText);
            probability.setText(probabilityOfDyingText);
            rounds.setVisibility(View.VISIBLE);
            probability.setVisibility(View.VISIBLE);

        }else{
            computer.isPlayerPlayingRussianRoulette = false;
        }

        setupTimer();
    }

    /**
     * Sets up the timer with options from settings
     */
    private void setupTimer() {
        TextView timerText = findViewById(R.id.timerText);
        ProgressBar countdown = findViewById(R.id.countdown);
        View timerContainer = findViewById(R.id.timerContainer);

        // Get settings
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        boolean timerEnabled = prefs.getBoolean("timer_enabled", true);

        if (timerEnabled) {
            // Get timer duration (default 7)
            int timerProgress = prefs.getInt("timer_duration", 4);
            int timerDuration = 3 + timerProgress; // 3-10 seconds

            customTimer = new CustomCountdownTimer(timerText, countdown, timerDuration, 100, new CustomCountdownTimer.OnTimerFinishListener() {
                @Override
                public void onTimerFinish() {
                    // Timer end logic
                    player.generateHands();
                    throwHands(null);
                }
            });

            customTimer.start();
            timerContainer.setVisibility(View.VISIBLE);
        } else {
            // Hide timer if disabled in settings
            timerContainer.setVisibility(View.GONE);
        }
    }

    /*
    Current game is limited to choosing different hands, players are not allowed to have both of their hands
    carry the same card (hand1: Rock, hand2: Rock -> is not allowed)
     */
    public void isChosen(View cardButtons){
        // Play sound effect for card selection
        soundManager.playCardSelect();

        // Add vibration if enabled
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        boolean vibrationEnabled = prefs.getBoolean("vibration_enabled", true);

        if (vibrationEnabled && vibrator != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            } catch (Exception ignored) {
                // Defensive fallback to prevent security crashes
            }
        }

        int tag = -1;
        if (cardButtons != null && cardButtons.getTag() != null) {
            try {
                tag = Integer.parseInt(cardButtons.getTag().toString());
            } catch (NumberFormatException ignored) {}
        }
        if (tag == -1) return;

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

        View rockBtn = findViewById(R.id.rockBG);
        View paperBtn = findViewById(R.id.cardButton1);
        View scissorBtn = findViewById(R.id.paperBG3);

        boolean rockSelected = false;
        boolean paperSelected = false;
        boolean scissorSelected = false;

        // Check selected hands
        if (player.getHand1() != null) {
            switch (player.getHand1().getHandNumber()) {
                case 1: rockSelected = true; break;
                case 2: paperSelected = true; break;
                case 3: scissorSelected = true; break;
            }
        }
        if (player.getHand2() != null) {
            switch (player.getHand2().getHandNumber()) {
                case 1: rockSelected = true; break;
                case 2: paperSelected = true; break;
                case 3: scissorSelected = true; break;
            }
        }

        if (rockHint != null) rockHint.setText(rockSelected ? "⭐" : "");
        if (paperHint != null) paperHint.setText(paperSelected ? "⭐" : "");
        if (scissorHint != null) scissorHint.setText(scissorSelected ? "⭐" : "");

        if (rockBtn != null) rockBtn.setSelected(rockSelected);
        if (paperBtn != null) paperBtn.setSelected(paperSelected);
        if (scissorBtn != null) scissorBtn.setSelected(scissorSelected);
    }

    public void forfeitGame(View forfeitButton) {
        // Play button click sound
        soundManager.playButtonClick();

        if (customTimer != null) {
            customTimer.cancel();
            customTimer = null;
        }
        finish();
    }

    public void throwHands(View throwButton){
        if(player.getHand1() == null || player.getHand2() == null){
            Toast.makeText(this, "Please choose two hands", Toast.LENGTH_SHORT).show();
        }else{
            // Play button click sound
            soundManager.playButtonClick();

            // Cancel the timer
            if (customTimer != null) {
                customTimer.cancel();
                customTimer = null;
            }

            // Stop all sounds before transition
            soundManager.stopAllSounds();

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
        // Stop all sounds when leaving the activity
        soundManager.stopAllSounds();
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
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class showFinalHands extends AppCompatActivity {
    private Player playerUser;
    private Computer_Player computerUser;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_final_hands);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize sound manager
        soundManager = SoundManager.getInstance(this);

        Player player = (Player) getIntent().getSerializableExtra("player");
        if (player == null) {
            player = new Player();
        }
        if (player.getFinalHand() == null) {
            player.setFinalHand(1);
        }

        Computer_Player computer = (Computer_Player) getIntent().getSerializableExtra("computer");
        if (computer == null) {
            computer = new Computer_Player();
        }
        if (computer.getFinalHand() == null) {
            computer.setFinalHand(2);
        }

        Russian_Roulette russianRoulette = (Russian_Roulette) getIntent().getSerializableExtra("russian_roulette");

        playerUser = player;
        computerUser = computer;

        // First, hide all card images and text
        hideCardContents();

        // Setup card images (but keep them hidden)
        setupCardImages(player, computer);

        // Record player's choice for AI learning
        if (computer instanceof Computer_Player) {
            ((Computer_Player) computer).recordPlayerChoice(player.getFinalHand().getHandNumber());
        }

        // Start the card entrance animation
        animateCardEntrance(findViewById(R.id.finalEnemyCard), findViewById(R.id.finalPlayerChosen));

        // Show card contents after animation completes
        new Handler().postDelayed(this::showCardContents, 1600); // Show after 1.6 seconds

        // Start the result countdown after cards are revealed
        new Handler().postDelayed(() -> {
            showResults(russianRoulette);
        }, 2200); // Increased delay to account for card reveal
    }

    private void hideCardContents() {
        // Hide enemy card content
        findViewById(R.id.finalEnemyChosen).setAlpha(0f);
        findViewById(R.id.enemyFinal).setAlpha(0f);

        // Hide player card content
        findViewById(R.id.finalPlayerCard).setAlpha(0f);
        findViewById(R.id.playerFinal).setAlpha(0f);
    }

    private void showCardContents() {
        // Show enemy card content with fade in
        findViewById(R.id.finalEnemyChosen).animate().alpha(1f).setDuration(500).start();
        findViewById(R.id.enemyFinal).animate().alpha(1f).setDuration(500).start();

        // Show player card content with fade in
        findViewById(R.id.finalPlayerCard).animate().alpha(1f).setDuration(500).start();
        findViewById(R.id.playerFinal).animate().alpha(1f).setDuration(500).start();

        // Optional: Add a popup effect when revealing
        findViewById(R.id.finalEnemyChosen).animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                .withEndAction(() -> findViewById(R.id.finalEnemyChosen).animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                .start();

        findViewById(R.id.finalPlayerCard).animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                .withEndAction(() -> findViewById(R.id.finalPlayerCard).animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                .start();
    }

    private void animateCardEntrance(View enemyCard, View playerCard) {
        // Enemy card slides from top
        enemyCard.setTranslationY(-1000f);
        enemyCard.animate()
                .translationY(0f)
                .setDuration(800)
                .start();

        // Player card slides from bottom
        playerCard.setTranslationY(1000f);
        playerCard.animate()
                .translationY(0f)
                .setDuration(800)
                .start();

        // Add rotation to both cards
        enemyCard.animate().rotation(720f).setDuration(1500).start();
        playerCard.animate().rotation(-720f).setDuration(1500).start();
    }

    private void setupCardImages(Player player, Computer_Player computer) {
        // Setup player card
        setupSingleCard(findViewById(R.id.finalPlayerCard),
                findViewById(R.id.playerFinal),
                player.getFinalHand());

        // Setup computer card
        setupSingleCard(findViewById(R.id.finalEnemyChosen),
                findViewById(R.id.enemyFinal),
                computer.getFinalHand());
    }

    private void setupSingleCard(ImageView imageView, TextView textView, Hand hand) {
        switch(hand.getHandNumber()) {
            case 1:
                textView.setText("Rock");
                imageView.setImageResource(R.drawable.rock);
                break;
            case 2:
                textView.setText("Paper");
                imageView.setImageResource(R.drawable.paper);
                break;
            case 3:
                textView.setText("Scissor");
                imageView.setImageResource(R.drawable.scissor);
                break;
        }
    }

    private void showResults(Russian_Roulette russianRoulette) {
        int playerFinalHand = playerUser.getFinalHand().getHandNumber();
        int computerFinalHand = computerUser.getFinalHand().getHandNumber();
        int result = (playerFinalHand - computerFinalHand + 3) % 3;

        TextView resultText = findViewById(R.id.round);

        // Animate result text appearance
        resultText.setAlpha(0f);
        resultText.setScaleX(0f);
        resultText.setScaleY(0f);

        if (result == 0) {
            handleTie(russianRoulette, resultText);
        } else {
            handleWinLose(result == 1, russianRoulette, resultText);
        }
    }

    private void handleTie(Russian_Roulette russianRoulette, TextView resultText) {
        resultText.setText(R.string.tie);
        resultText.setTextColor(getColor(R.color.paragraph));

        // Play tie sound
        soundManager.playTie();

        animateResult(resultText);

        if (playerUser.isPlayerPlayingRussianRoulette) {
            showTieWarning(russianRoulette);
        } else {
            StatsActivity.updateStats(this, "normal_ties");
            showEndButtons();
        }
    }

    private void handleWinLose(boolean didPlayerWin, Russian_Roulette russianRoulette, TextView resultText) {
        playerUser.didPlayerWin = didPlayerWin;
        resultText.setText(didPlayerWin ? R.string.win : R.string.lose);
        resultText.setTextColor(didPlayerWin ? getColor(R.color.buttonColor) : getColor(R.color.red));

        // Play win or lose sound
        if (didPlayerWin) {
            soundManager.playWin();
        } else {
            soundManager.playLose();
        }

        animateResult(resultText);

        if (playerUser.isPlayerPlayingRussianRoulette) {
            handleRussianRouletteResult(russianRoulette);
        } else {
            updateNormalStats(didPlayerWin);
            showEndButtons();
        }
    }

    private void animateResult(TextView resultText) {
        resultText.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> {
                    resultText.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void showTieWarning(Russian_Roulette russianRoulette) {
        TextView warning = findViewById(R.id.TieWarning);
        warning.setVisibility(View.VISIBLE);
        warning.setAlpha(0f);
        warning.animate().alpha(1f).setDuration(500).start();

        new Handler().postDelayed(() -> {
            // Stop all sounds before transition
            soundManager.stopAllSounds();

            Intent intent = new Intent(this, NormalMode.class);
            intent.putExtra("player", playerUser);
            intent.putExtra("computer", computerUser);
            intent.putExtra("russian_roulette", russianRoulette);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 1300);
    }

    private void handleRussianRouletteResult(Russian_Roulette russianRoulette) {
        new Handler().postDelayed(() -> {
            if (russianRoulette.fireRound()) {
                // Stop all sounds before transition
                soundManager.stopAllSounds();

                Class<?> targetActivity = russianRoulette.isShotDeadly() ? LiveFireGun.class : blankFireGun.class;

                if (russianRoulette.isShotDeadly()) {
                    StatsActivity.updateStats(this, playerUser.didPlayerWin ? "russian_wins" : "russian_deaths");
                } else {
                    StatsActivity.updateStats(this, "russian_survived");
                }

                Intent intent = new Intent(this, targetActivity);
                intent.putExtra("didWin", playerUser.didPlayerWin);
                intent.putExtra("player", playerUser);
                intent.putExtra("computer", computerUser);
                intent.putExtra("russian_roulette", russianRoulette);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 1300);
    }

    private void updateNormalStats(boolean didPlayerWin) {
        String statType = didPlayerWin ? "normal_wins" : "normal_losses";
        StatsActivity.updateStats(this, statType);
    }

    private void showEndButtons() {
        View[] buttons = {
                findViewById(R.id.tryAgain),
                findViewById(R.id.playRR),
                findViewById(R.id.exit)
        };

        for (int i = 0; i < buttons.length; i++) {
            final View button = buttons[i];
            button.setVisibility(View.VISIBLE);
            button.setAlpha(0f);
            button.setTranslationY(50f);

            button.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 100)
                    .setDuration(400)
                    .start();
        }
    }

    public void exit(View view) {
        // Play button click sound
        soundManager.playButtonClick();

        // Stop all sounds
        soundManager.stopAllSounds();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void tryAgain(View view) {
        // Play button click sound
        soundManager.playButtonClick();

        // Stop all sounds
        soundManager.stopAllSounds();

        Intent intent = new Intent(this, LoadingScreenActivity.class);
        playerUser = null;
        computerUser = null;
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = false;
        intent.putExtra("player", player);
        startActivity(intent);
        finish();
    }

    public void playRR(View view) {
        // Play button click sound
        soundManager.playButtonClick();

        // Stop all sounds
        soundManager.stopAllSounds();

        Intent intent = new Intent(this, RussianModeLoadingScreen.class);
        Player player = new Player();
        player.isPlayerPlayingRussianRoulette = true;
        intent.putExtra("player", player);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop all sounds when leaving the activity
        soundManager.stopAllSounds();
    }

}
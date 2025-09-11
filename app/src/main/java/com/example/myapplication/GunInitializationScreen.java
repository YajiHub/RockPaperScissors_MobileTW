package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GunInitializationScreen extends AppCompatActivity {

    // Animation properties
    private ImageView gunImageView;
    private Handler animationHandler;
    private int currentFrame = 0;
    private int[] gunAnimationFrames;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gun_initialization_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the ImageView
        gunImageView = findViewById(R.id.gunImageView);

        // Initialize sound manager
        soundManager = SoundManager.getInstance(this);

        gunAnimationFrames = new int[] {
                R.drawable.frame01,
                R.drawable.frame02,
                R.drawable.frame03,
                R.drawable.frame04,
                R.drawable.frame05,
                R.drawable.frame06,
                R.drawable.frame07,
                R.drawable.frame08,
                R.drawable.frame09,
                R.drawable.frame10
        };

        // Start the animation
        startAnimation();

        // Move to next activity when animation completes
        int totalDuration = calculateTotalDuration();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Stop the animation
                stopAnimation();

                // Stop all sounds before transition
                soundManager.stopAllSounds();

                // Move to next activity
                Intent intent = new Intent(GunInitializationScreen.this, NormalMode.class);
                Russian_Roulette russian_roulette = new Russian_Roulette();
                russian_roulette.gunInitialize();
                intent.putExtra("russian_roulette", russian_roulette);
                Player player = new Player();
                player.isPlayerPlayingRussianRoulette = true;
                intent.putExtra("player", player);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, totalDuration);
    }

    /**
     * Calculate total duration of the animation
     */
    private int calculateTotalDuration() {
        // Different timings for different parts of the animation
        int loadingDuration = 600; // Frames 0-2 (loading bullet)
        int closingDuration = 400; // Frames 3-4 (closing cylinder)
        int spinningDuration = 1000; // Frames 5-7 (spinning)
        int cockingDuration = 400; // Frames 8-9 (cocking hammer)

        return loadingDuration + closingDuration + spinningDuration + cockingDuration;
    }

    /**
     * Starts the frame animation
     */
    private void startAnimation() {
        animationHandler = new Handler();
        runAnimation();
    }

    /**
     * Runs the animation by cycling through the frames
     */
    private void runAnimation() {
        if (currentFrame < gunAnimationFrames.length) {
            // Display the current frame
            gunImageView.setImageResource(gunAnimationFrames[currentFrame]);

            // Play appropriate sound effect based on the frame
            playSoundForFrame(currentFrame);

            // Increment frame counter
            currentFrame++;

            // Schedule next frame with appropriate delay
            int delay = getDelayForFrame(currentFrame);
            animationHandler.postDelayed(this::runAnimation, delay);
        }
    }

    /**
     * Determine the appropriate delay for each frame
     */
    private int getDelayForFrame(int frameIndex) {
        // Slower for loading frames (0-2)
        if (frameIndex < 3) {
            return 200; // 200ms delay
        }
        // Medium for closing cylinder (3-4)
        else if (frameIndex < 5) {
            return 200; // 200ms delay
        }
        // Fast for spinning frames (5-7)
        else if (frameIndex < 8) {
            return 100; // 100ms delay (faster)
        }
        // Normal for hammer cocking (8-9)
        else {
            return 200; // 200ms delay
        }
    }

    /**
     * Play appropriate sound effect for each frame
     */
    private void playSoundForFrame(int frameIndex) {
        switch (frameIndex) {
            case 0: // First frame - empty gun
                // No sound
                break;
            case 1: // Loading bullet
                soundManager.playGunLoad();
                break;
            case 4: // Cylinder closed
                soundManager.playButtonClick(); // Use as cylinder click sound
                break;
            case 5: // Start spinning
                soundManager.playGunSpin();
                break;
            case 8: // Hammer cocking
                soundManager.playGunCock();
                break;
            default:
                // No sound for other frames
                break;
        }
    }

    /**
     * Stops the animation
     */
    private void stopAnimation() {
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * When activity is paused, stop all sounds
     */
    @Override
    protected void onPause() {
        super.onPause();
        soundManager.stopAllSounds();
    }

    /**
     * Clean up when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnimation();
        soundManager.stopAllSounds();
    }
}

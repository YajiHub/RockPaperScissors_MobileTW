package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.SparseIntArray;

/**
 * Manages all sound effects in the game
 * Uses the Singleton pattern for easy access throughout the game
 */
public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private boolean soundEnabled = true;
    private Context context;
    private boolean loaded = false;

    // Track playing sounds
    private SparseIntArray streamIds = new SparseIntArray();

    // Sound IDs
    private int buttonClickSound;
    private int cardSelectSound;
    private int winSound;
    private int loseSound;
    private int tieSound;
    private int gunCockSound;
    private int gunSpinSound;
    private int gunEmptySound;
    private int gunLoadSound;

    // Private constructor (Singleton pattern)
    private SoundManager(Context context) {
        this.context = context.getApplicationContext();

        // Load sound setting from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE);
        soundEnabled = prefs.getBoolean("sound_enabled", true);

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        // Set up listener to know when sounds are loaded
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            loaded = true;
        });

        // Load sound effects
        loadSounds();
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Load all sound effects
     */
    private void loadSounds() {
        // Load your sound files from the raw folder
        try {
            // UI Sounds
            buttonClickSound = soundPool.load(context, R.raw.button_click, 1);
            cardSelectSound = soundPool.load(context, R.raw.card_select, 1);

            // Game outcome sounds
            winSound = soundPool.load(context, R.raw.win, 1);
            loseSound = soundPool.load(context, R.raw.lose, 1);
            tieSound = soundPool.load(context, R.raw.tie, 1);

            // Gun sounds for Russian Roulette
            gunCockSound = soundPool.load(context, R.raw.gun_cock, 1);
            gunSpinSound = soundPool.load(context, R.raw.gun_spin, 1);
            gunEmptySound = soundPool.load(context, R.raw.gun_empty, 1);
            gunLoadSound = soundPool.load(context, R.raw.gun_load, 1);

        } catch (Exception e) {
            // In case a sound file is missing, just continue
            e.printStackTrace();
        }
    }

    /**
     * Update the sound enabled setting
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;

        if (!enabled) {
            // Stop all sounds if sound is disabled
            stopAllSounds();
        }

        // Save the setting to SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("sound_enabled", enabled);
        editor.apply();
    }

    /**
     * Check if sound is enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Play a sound with the given ID
     * @param soundId the ID of the sound to play
     * @return the stream ID of the sound, or 0 if sound is disabled or failed
     */
    private int playSound(int soundId) {
        if (soundEnabled && loaded && soundId > 0) {
            int streamId = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);

            // Store the stream ID for tracking
            if (streamId != 0) {
                streamIds.put(soundId, streamId);
            }

            return streamId;
        }
        return 0;
    }

    /**
     * Stop a specific sound
     * @param soundId the sound resource ID to stop
     */
    public void stopSound(int soundId) {
        int streamId = streamIds.get(soundId, 0);
        if (streamId != 0) {
            soundPool.stop(streamId);
            streamIds.delete(soundId);
        }
    }

    /**
     * Stop all currently playing sounds
     */
    public void stopAllSounds() {
        for (int i = 0; i < streamIds.size(); i++) {
            int streamId = streamIds.valueAt(i);
            if (streamId != 0) {
                soundPool.stop(streamId);
            }
        }
        streamIds.clear();
    }

    // Methods to play specific sounds

    /**
     * Play button click sound
     */
    public void playButtonClick() {
        playSound(buttonClickSound);
    }

    /**
     * Play card select sound
     */
    public void playCardSelect() {
        playSound(cardSelectSound);
    }

    /**
     * Play win sound
     */
    public void playWin() {
        stopAllSounds();
        playSound(winSound);
    }

    /**
     * Play lose sound
     */
    public void playLose() {
        stopAllSounds();
        playSound(loseSound);
    }

    /**
     * Play tie sound
     */
    public void playTie() {
        stopAllSounds();
        playSound(tieSound);
    }

    /**
     * Play gun cocking sound
     */
    public void playGunCock() {
        stopSound(gunSpinSound);
        playSound(gunCockSound);
    }

    /**
     * Play gun cylinder spinning sound
     */
    public void playGunSpin() {
        stopSound(gunLoadSound);
        playSound(gunSpinSound);
    }

    /**
     * Play empty gun shot sound
     */
    public void playGunEmpty() {
        stopAllSounds();
        playSound(gunEmptySound);
    }

    /**
     * Play gun loading sound
     */
    public void playGunLoad() {
        stopAllSounds();
        playSound(gunLoadSound);
    }

    /**
     * Release all resources
     * Call this in onDestroy of your main activity
     */
    public void release() {
        if (soundPool != null) {
            stopAllSounds();
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}
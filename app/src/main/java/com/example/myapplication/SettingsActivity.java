package com.example.myapplication;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Switch soundSwitch, vibrationSwitch, timerSwitch;
    private SeekBar volumeSeekBar, timerSeekBar;
    private TextView volumeText, timerText;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        initializeViews();
        loadSettings();
    }

    private void initializeViews() {
        soundSwitch = findViewById(R.id.soundSwitch);
        vibrationSwitch = findViewById(R.id.vibrationSwitch);
        timerSwitch = findViewById(R.id.timerSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        timerSeekBar = findViewById(R.id.timerSeekBar);
        volumeText = findViewById(R.id.volumeText);
        timerText = findViewById(R.id.timerText);

        // Set up volume control listener
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeText.setText(progress + "%");

                if (fromUser) {
                    // Set the system volume
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int newVolume = (progress * maxVolume) / 100;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set up timer duration listener
        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Timer range: 3-10 seconds (progress 0-7 maps to 3-10)
                int time = 3 + progress;
                timerText.setText(time + " seconds");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadSettings() {
        // Load switch states (default values: all true)
        soundSwitch.setChecked(prefs.getBoolean("sound_enabled", true));
        vibrationSwitch.setChecked(prefs.getBoolean("vibration_enabled", true));
        timerSwitch.setChecked(prefs.getBoolean("timer_enabled", true));

        // Load volume setting (default: 80%)
        int volume = prefs.getInt("volume", 80);
        volumeSeekBar.setProgress(volume);
        volumeText.setText(volume + "%");

        // Set actual system volume based on saved setting
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = (volume * maxVolume) / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);

        // Load timer setting (default: 7 seconds, which is progress 4)
        int timer = prefs.getInt("timer_duration", 4);
        timerSeekBar.setProgress(timer);
        timerText.setText((3 + timer) + " seconds");
    }

    public void saveSettings(View view) {
        SharedPreferences.Editor editor = prefs.edit();

        // Save switch states
        boolean soundEnabled = soundSwitch.isChecked();
        editor.putBoolean("sound_enabled", soundEnabled);
        editor.putBoolean("vibration_enabled", vibrationSwitch.isChecked());
        editor.putBoolean("timer_enabled", timerSwitch.isChecked());

        // Save volume and timer settings
        editor.putInt("volume", volumeSeekBar.getProgress());
        editor.putInt("timer_duration", timerSeekBar.getProgress());

        // Apply changes immediately
        editor.apply();

        // Update the sound manager with new setting
        SoundManager.getInstance(this).setSoundEnabled(soundEnabled);

        // Show confirmation
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();

        // Play button click sound
        SoundManager.getInstance(this).playButtonClick();

        // Return to previous screen
        finish();
    }

    public void goBack(View view) {
        finish();
    }

    // Static helper methods for other activities
    public static boolean isSoundEnabled(AppCompatActivity activity) {
        return activity.getSharedPreferences("GameSettings", MODE_PRIVATE)
                .getBoolean("sound_enabled", true);
    }

    public static boolean isVibrationEnabled(AppCompatActivity activity) {
        return activity.getSharedPreferences("GameSettings", MODE_PRIVATE)
                .getBoolean("vibration_enabled", true);
    }

    public static int getTimerDuration(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("GameSettings", MODE_PRIVATE);
        int timerProgress = prefs.getInt("timer_duration", 4);
        return 3 + timerProgress; // Convert to actual seconds (3-10)
    }

    public static boolean isTimerEnabled(AppCompatActivity activity) {
        return activity.getSharedPreferences("GameSettings", MODE_PRIVATE)
                .getBoolean("timer_enabled", true);
    }
}
package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StatsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView normalWins, normalLosses, normalTies;
    private TextView russianSurvived, russianDeaths, russianWins;
    private TextView totalGamesPlayed, winRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("GameStats", MODE_PRIVATE);
        initializeViews();
        displayStats();
    }

    private void initializeViews() {
        normalWins = findViewById(R.id.normalWins);
        normalLosses = findViewById(R.id.normalLosses);
        normalTies = findViewById(R.id.normalTies);
        russianSurvived = findViewById(R.id.russianSurvived);
        russianDeaths = findViewById(R.id.russianDeaths);
        russianWins = findViewById(R.id.russianWins);
        totalGamesPlayed = findViewById(R.id.totalGamesPlayed);
        winRate = findViewById(R.id.winRate);
    }

    private void displayStats() {
        int nWins = prefs.getInt("normal_wins", 0);
        int nLosses = prefs.getInt("normal_losses", 0);
        int nTies = prefs.getInt("normal_ties", 0);
        int rSurvived = prefs.getInt("russian_survived", 0);
        int rDeaths = prefs.getInt("russian_deaths", 0);
        int rWins = prefs.getInt("russian_wins", 0);

        normalWins.setText(String.valueOf(nWins));
        normalLosses.setText(String.valueOf(nLosses));
        normalTies.setText(String.valueOf(nTies));
        russianSurvived.setText(String.valueOf(rSurvived));
        russianDeaths.setText(String.valueOf(rDeaths));
        russianWins.setText(String.valueOf(rWins));

        int total = nWins + nLosses + nTies + rSurvived + rDeaths + rWins;
        totalGamesPlayed.setText(String.valueOf(total));

        double rate = total > 0 ? ((double)(nWins + rWins) / total) * 100 : 0;
        winRate.setText(String.format("%.1f%%", rate));
    }

    public void resetStats(View view) {
        prefs.edit().clear().apply();
        displayStats();
    }

    public void goBack(View view) {
        finish();
    }

    // Fixed method to accept AppCompatActivity instead of MainActivity
    public static void updateStats(AppCompatActivity activity, String statType) {
        SharedPreferences prefs = activity.getSharedPreferences("GameStats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int currentValue = prefs.getInt(statType, 0);
        editor.putInt(statType, currentValue + 1);
        editor.apply();
    }
}
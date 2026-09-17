package com.example.myapplication;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class Russian_Roulette implements Serializable {
    /*
    Round	Probability of Death	Percentage
    1	    1/6                    	≈16.67%
    2	    1/5	                    20%
    3	    1/4	                    25%
    4	    1/3	                    ≈33.33%
    5	    1/2	                    50%
    6	    1/1	                    100%
     */

    private final static Random random = new Random();
    private int currentRound;
    private int bullet = 1;
    private int[] rounds = new int[]{0, 0, 0, 0, 0, 0};
    private int bulletPosition;
    private final int totalRounds = 6;
    private boolean isBulletFired;

    public void gunInitialize() {
        rounds = new int[totalRounds];
        currentRound = 0;
        bulletPosition = random.nextInt(totalRounds);
        rounds[bulletPosition] = bullet;
        isBulletFired = false;
    }

    public boolean fireRound() {
        if (currentRound < totalRounds) {
            isBulletFired = (rounds[currentRound] == 1);
            currentRound++;
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public boolean isShotDeadly() {
        return isBulletFired;
    }

    public double getDeathProbability() {
        int remaining = totalRounds - currentRound;
        return remaining > 0 ? (1.0 / remaining) : 0;
    }

    public String getFormattedProbability() {
        return String.format("%.2f%%", getDeathProbability() * 100);
    }
}

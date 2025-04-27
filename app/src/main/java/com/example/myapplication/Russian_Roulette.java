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
    static int bullet = 1;
    static int[] rounds = {0,0,0,0,0,0};
    static int bulletPosition;
    final int totalRounds = 6;
    static boolean isBulletFired;

    public void gunInitialize(){
        Arrays.fill(rounds, 0);
        currentRound = 0;
        bulletPosition = random.nextInt(6);
        rounds[bulletPosition] = bullet;
    }


    public boolean fireRound(){
        if (currentRound < totalRounds) {
            if (rounds[currentRound] == 1) {
                isBulletFired = true;
            } else {
                isBulletFired = false;
            }
            currentRound++;
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentRound(){
        return currentRound;
    }

    public int getTotalRounds(){
        return totalRounds;
    }

    public boolean isShotDeadly(){
        return isBulletFired;
    }




}

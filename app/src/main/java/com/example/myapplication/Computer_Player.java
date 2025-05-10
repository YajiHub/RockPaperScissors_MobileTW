package com.example.myapplication;

import java.util.Random;

public class Computer_Player extends Player {
    static Random random = new Random();

    /**
     * Returns the hand that would beat the given hand
     */
    public Hand getCounterHand(Hand hand) {
        switch(hand) {
            case ROCK:
                return Hand.PAPER;
            case PAPER:
                return Hand.SCISSOR;
            case SCISSOR:
                return Hand.ROCK;
        }
        return null;
    }

    /**
     * Randomly chooses between two hands
     */
    public Hand chooseRandomHand(Hand hand1, Hand hand2) {
        Hand[] hand = new Hand[2];
        hand[0] = hand1;
        hand[1] = hand2;
        int index = random.nextInt(2);
        return hand[index];
    }

    @Override
    public void pickFinalHand(Hand playerHand1, Hand playerHand2) {
        Hand thisHand1 = this.getHand1();
        Hand thisHand2 = this.getHand2();

        // If computer has same hand for both choices, no decision needed
        if (thisHand1 == thisHand2) {
            super.setFinalHand(thisHand1.getHandNumber());
            return;
        }

        // If player has the same hand for both choices, try to counter it
        if (playerHand1 == playerHand2) {
            Hand strongestCounter = getCounterHand(playerHand1);
            if (thisHand1 == strongestCounter) {
                super.setFinalHand(thisHand1.getHandNumber());
                return;
            } else if (thisHand2 == strongestCounter) {
                super.setFinalHand(thisHand2.getHandNumber());
                return;
            }
            // If we can't counter, we'll fall through to the strategic choice below
        }

        // Strategic decision making when player has different hands or we don't have a direct counter

        // Calculate scores for each of our hands against the player's hands
        int hand1Score = evaluateHand(thisHand1, playerHand1, playerHand2);
        int hand2Score = evaluateHand(thisHand2, playerHand1, playerHand2);

        // Choose the hand with the better score
        if (hand1Score > hand2Score) {
            super.setFinalHand(thisHand1.getHandNumber());
        } else if (hand2Score > hand1Score) {
            super.setFinalHand(thisHand2.getHandNumber());
        } else {
            // Scores are equal, choose randomly
            super.setFinalHand(chooseRandomHand(thisHand1, thisHand2).getHandNumber());
        }
    }

    /**
     * Evaluates a hand against both player hands and returns a score
     * Win = +1, Tie = 0, Loss = -1
     * Best possible score is +2 (wins against both player hands)
     * Worst possible score is -2 (loses to both player hands)
     */
    private int evaluateHand(Hand computerHand, Hand playerHand1, Hand playerHand2) {
        return compareHands(computerHand, playerHand1) + compareHands(computerHand, playerHand2);
    }

    /**
     * Compares two hands and returns:
     * +1 if computer hand wins
     * 0 if it's a tie
     * -1 if computer hand loses
     */
    private int compareHands(Hand computerHand, Hand playerHand) {
        if (computerHand == playerHand) {
            return 0; // Tie
        } else if ((computerHand == Hand.ROCK && playerHand == Hand.SCISSOR) ||
                (computerHand == Hand.SCISSOR && playerHand == Hand.PAPER) ||
                (computerHand == Hand.PAPER && playerHand == Hand.ROCK)) {
            return 1; // Computer wins
        } else {
            return -1; // Computer loses
        }
    }
}
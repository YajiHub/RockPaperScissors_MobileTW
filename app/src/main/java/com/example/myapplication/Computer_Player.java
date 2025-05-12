package com.example.myapplication;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Computer_Player extends Player {
    static Random random = new Random();

    // Track game history to identify player patterns
    private static ArrayList<Integer> playerHistory = new ArrayList<>();
    private static HashMap<Integer, Integer> playerFinalChoiceFrequency = new HashMap<>();

    // Difficulty levels (0-100, higher is smarter)
    private int difficultyLevel = 95; // Default high difficulty

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

    public void setDifficultyLevel(int level) {
        this.difficultyLevel = level;
    }

    /**
     * Returns the hand that would lose to the given hand
     * (Used for intentionally losing sometimes)
     */
    public Hand getWeakHand(Hand hand) {
        switch(hand) {
            case ROCK:
                return Hand.SCISSOR;
            case PAPER:
                return Hand.ROCK;
            case SCISSOR:
                return Hand.PAPER;
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

    /**
     * Record the player's choice to identify patterns
     */
    public void recordPlayerChoice(int handNumber) {
        playerHistory.add(handNumber);

        // Update frequency map
        Integer count = playerFinalChoiceFrequency.get(handNumber);
        if (count == null) count = 0;
        playerFinalChoiceFrequency.put(handNumber, count + 1);
    }

    /**
     * Predict player's likely next move based on history
     * Returns null if not enough history or no clear pattern
     */
    private Hand predictPlayerMove() {
        if (playerHistory.size() < 3) return null; // Need history to predict

        // Find most frequent hand
        int maxCount = 0;
        int likelyChoice = 0;

        for (Integer choice : playerFinalChoiceFrequency.keySet()) {
            int frequency = playerFinalChoiceFrequency.get(choice);
            if (frequency > maxCount) {
                maxCount = frequency;
                likelyChoice = choice;
            }
        }

        // If there's a clear preference (used over 50% of the time)
        if (maxCount > playerHistory.size() / 2) {
            switch(likelyChoice) {
                case 1: return Hand.ROCK;
                case 2: return Hand.PAPER;
                case 3: return Hand.SCISSOR;
            }
        }

        // Check for simple patterns like alternating hands or repeating sequences
        if (playerHistory.size() >= 4) {
            // Check if player alternates between two choices
            int last = playerHistory.get(playerHistory.size() - 1);
            int secondLast = playerHistory.get(playerHistory.size() - 2);
            int thirdLast = playerHistory.get(playerHistory.size() - 3);

            if (last == thirdLast && secondLast != last) {
                // Alternating pattern detected
                return getHandFromNumber(last);
            }
        }

        return null; // No clear pattern
    }

    private Hand getHandFromNumber(int number) {
        switch(number) {
            case 1: return Hand.ROCK;
            case 2: return Hand.PAPER;
            case 3: return Hand.SCISSOR;
            default: return null;
        }
    }

    @Override
    public void pickFinalHand(Hand playerHand1, Hand playerHand2) {
        Hand thisHand1 = this.getHand1();
        Hand thisHand2 = this.getHand2();

        // Occasionally play suboptimally to avoid being too predictable
        if (random.nextInt(100) > difficultyLevel) {
            Hand randomChoice = chooseRandomHand(thisHand1, thisHand2);
            super.setFinalHand(randomChoice.getHandNumber());
            return;
        }

        // If computer has same hand for both choices, no decision needed
        if (thisHand1 == thisHand2) {
            super.setFinalHand(thisHand1.getHandNumber());
            return;
        }

        // Try to predict player's move
        Hand predictedPlayerMove = predictPlayerMove();

        // If we have a prediction with high confidence
        if (predictedPlayerMove != null) {
            // Counter the predicted move
            Hand counterHand = getCounterHand(predictedPlayerMove);

            if (thisHand1 == counterHand) {
                super.setFinalHand(thisHand1.getHandNumber());
                return;
            } else if (thisHand2 == counterHand) {
                super.setFinalHand(thisHand2.getHandNumber());
                return;
            }
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
        }

        // Strategic decision making when player has different hands

        // Calculate scores for each of our hands against the player's hands
        int hand1Score = evaluateHand(thisHand1, playerHand1, playerHand2);
        int hand2Score = evaluateHand(thisHand2, playerHand1, playerHand2);

        // Add a weighted random factor to make decisions less predictable
        if (random.nextInt(100) < 30) {
            hand1Score += random.nextInt(2) - 1;
            hand2Score += random.nextInt(2) - 1;
        }

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
     * Set the difficulty level (0-100)
     * 0 = completely random
     * 100 = plays optimally
     */
    public void setDifficulty(int level) {
        difficultyLevel = Math.max(0, Math.min(100, level));
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
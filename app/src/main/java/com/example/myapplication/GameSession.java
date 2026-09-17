package com.example.myapplication;

import java.io.Serializable;

/**
 * Lifecycle-safe Game Session Manager
 * Holds in-flight game state and logic to prevent static memory leaks across Activities.
 */
public class GameSession implements Serializable {

    private static GameSession instance;

    private Player player;
    private Computer_Player computer;
    private Russian_Roulette russianRoulette;
    private boolean isRussianRoulette;
    private int currentRound;
    private int playerScore;
    private int computerScore;

    private GameSession() {
        reset();
    }

    public static synchronized GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }

    public void startNewGame(boolean russianMode) {
        this.isRussianRoulette = russianMode;
        this.currentRound = 1;
        this.playerScore = 0;
        this.computerScore = 0;

        this.player = new Player();
        this.player.isPlayerPlayingRussianRoulette = russianMode;

        this.computer = new Computer_Player();
        this.computer.isPlayerPlayingRussianRoulette = russianMode;

        this.russianRoulette = new Russian_Roulette();
        if (russianMode) {
            this.russianRoulette.gunInitialize();
        }
    }

    public void resetRound() {
        if (player != null) {
            player.hand1 = null;
            player.hand2 = null;
            player.finalHand = null;
        }
        if (computer != null) {
            computer.hand1 = null;
            computer.hand2 = null;
            computer.finalHand = null;
        }
    }

    public void reset() {
        startNewGame(false);
    }

    public Player getPlayer() {
        if (player == null) player = new Player();
        return player;
    }

    public Computer_Player getComputer() {
        if (computer == null) computer = new Computer_Player();
        return computer;
    }

    public Russian_Roulette getRussianRoulette() {
        if (russianRoulette == null) {
            russianRoulette = new Russian_Roulette();
            russianRoulette.gunInitialize();
        }
        return russianRoulette;
    }

    public boolean isRussianRoulette() {
        return isRussianRoulette;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void advanceRound() {
        this.currentRound++;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void incrementPlayerScore() {
        this.playerScore++;
    }

    public int getComputerScore() {
        return computerScore;
    }

    public void incrementComputerScore() {
        this.computerScore++;
    }
}

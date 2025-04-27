package com.example.myapplication;

public enum Hand {
    ROCK(1), PAPER(2), SCISSOR(3);

    private final int handNumber;

    Hand(int handNumber) {
        this.handNumber = handNumber;
    }

    public int getHandNumber(){
        return this.handNumber;
    }

}

package com.example.myapplication;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable{
    static Random random = new Random();
    boolean isDead = false;
    boolean isPlayerPlayingRussianRoulette = false;
    boolean didPlayerWin;
    String name;
    Hand hand1;
    Hand hand2;
    Hand finalHand;

    public void generateHands(){
        int hand1 = random.nextInt(3) + 1;
        int hand2 = random.nextInt(3) + 1;

        do{
            hand2 = random.nextInt(3) + 1;
        } while(hand1 == hand2);

        this.setPlayerHands(hand1, hand2);
    }

    public void setPlayerHands(int handNumber1, int handNumber2){
        switch(handNumber1){
            case 1:
                this.hand1 = Hand.ROCK;
                break;
            case 2:
                this.hand1 = Hand.PAPER;
                break;
            case 3:
                this.hand1 = Hand.SCISSOR;
                break;
        }
        switch(handNumber2){
            case 1:
                this.hand2 = Hand.ROCK;
                break;
            case 2:
                this.hand2 = Hand.PAPER;
                break;
            case 3:
                this.hand2 = Hand.SCISSOR;
                break;
        }
    }

    public Hand getHand1(){
        return this.hand1;
    }

    public Hand getHand2(){
        return this.hand2;
    }

    public void setFinalHand(int handNumber){
        switch(handNumber){
            case 1:
                this.finalHand = Hand.ROCK;
                break;
            case 2:
                this.finalHand = Hand.PAPER;
                break;
            case 3:
                this.finalHand = Hand.SCISSOR;
                break;
        }
    }

    public void setHand1(int tag){
        switch(tag){
            case 1:
                this.hand1 = Hand.ROCK;
                break;
            case 2:
                this.hand1 = Hand.PAPER;
                break;
            case 3:
                this.hand1 = Hand.SCISSOR;
                break;
        }
    }
    public void setHand2(int tag){
        switch(tag){
            case 1:
                this.hand2 = Hand.ROCK;
                break;
            case 2:
                this.hand2 = Hand.PAPER;
                break;
            case 3:
                this.hand2 = Hand.SCISSOR;
                break;
        }
    }

    public void pickFinalHand(Hand hand1, Hand hand2){
        Hand[] hand = {hand1, hand2};
        int index = random.nextInt(2);
        this.finalHand = hand[index];
    }

    public Hand getFinalHand(){
        return this.finalHand;
    }

}

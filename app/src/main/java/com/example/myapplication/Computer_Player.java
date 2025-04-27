package com.example.myapplication;

import java.util.Random;

public class Computer_Player extends Player{
    static Random random = new Random();


    public Hand getCounterHand(Hand hand){
        switch(hand){
            case ROCK:
                return Hand.PAPER;
            case PAPER:
                return Hand.SCISSOR;
            case SCISSOR:
                return Hand.ROCK;
        }
        return null;
    }

    public Hand chooseRandomHand(Hand hand1, Hand hand2){
        Hand[] hand = new Hand[2];
        hand[0] = hand1;
        hand[1] = hand2;
        int index = random.nextInt(2);
        return hand[index];
    }

//    @Override
//    public void generateHands(){
//        int hand1 = random.nextInt(3) + 1;
//        int hand2 = random.nextInt(3) + 1;
//
//        super.setPlayerHands(hand1, hand2);
//    }


    @Override
    public void pickFinalHand(Hand playerHand1, Hand playerHand2){
        int thisHand1 = this.getHand1().getHandNumber();
        int thisHand2 = this.getHand2().getHandNumber();
        if(thisHand1 == thisHand2){
            super.setFinalHand(thisHand1);
            return;
        //closing of if
        }
        if(playerHand1.getHandNumber() == playerHand2.getHandNumber()){
            Hand strongestCounter = this.getCounterHand(playerHand1);
            if(this.getHand1() == strongestCounter){
                super.setFinalHand(thisHand1);
            }
            else if(this.getHand2() == strongestCounter){
                super.setFinalHand(thisHand2);
            }else{
                super.setFinalHand(chooseRandomHand(this.getHand1(), this.getHand2()).getHandNumber());
            }
        //closing of if
        }

        super.setFinalHand(chooseRandomHand(this.getHand1(), this.getHand2()).getHandNumber());

        //closing of method
    }




}

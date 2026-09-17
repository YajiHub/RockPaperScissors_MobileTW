package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameLogicTest {

    private Russian_Roulette roulette1;
    private Russian_Roulette roulette2;
    private Computer_Player computer;
    private Player player;

    @Before
    public void setUp() {
        roulette1 = new Russian_Roulette();
        roulette2 = new Russian_Roulette();
        computer = new Computer_Player();
        player = new Player();
    }

    @Test
    public void testRouletteInstanceIsolation() {
        roulette1.gunInitialize();
        roulette2.gunInitialize();

        // Fire round on roulette1 only
        roulette1.fireRound();

        // With non-static state, roulette1 round advances while roulette2 stays at 0
        assertEquals("Roulette1 should be at round 1", 1, roulette1.getCurrentRound());
        assertEquals("Roulette2 must NOT share state with Roulette1", 0, roulette2.getCurrentRound());
    }

    @Test
    public void testRouletteDeathProbability() {
        roulette1.gunInitialize();
        assertEquals(6, roulette1.getTotalRounds());
        assertEquals(0, roulette1.getCurrentRound());

        // Fire rounds until all 6 rounds exhausted
        int totalFired = 0;
        int liveShots = 0;
        while (roulette1.fireRound()) {
            totalFired++;
            if (roulette1.isShotDeadly()) {
                liveShots++;
            }
        }

        assertEquals(6, totalFired);
        assertEquals("There must be exactly 1 live bullet in cylinder", 1, liveShots);
        assertFalse("Cannot fire past total chambers", roulette1.fireRound());
    }

    @Test
    public void testHandClashes() {
        player.setFinalHand(1); // ROCK
        computer.setFinalHand(3); // SCISSORS
        // Rock beats scissors
        assertTrue(player.getFinalHand() == Hand.ROCK);
        assertTrue(computer.getFinalHand() == Hand.SCISSOR);

        // Counter hand verification
        assertEquals(Hand.PAPER, computer.getCounterHand(Hand.ROCK));
        assertEquals(Hand.SCISSOR, computer.getCounterHand(Hand.PAPER));
        assertEquals(Hand.ROCK, computer.getCounterHand(Hand.SCISSOR));
    }

    @Test
    public void testDeadlyShotNotOverwrittenBySecondInstance() {
        roulette1.gunInitialize();
        while (roulette1.fireRound() && !roulette1.isShotDeadly()) {
            // advance until live round is fired
        }
        assertTrue("roulette1 must have fired deadly shot", roulette1.isShotDeadly());

        // Initialize and fire on second gun
        roulette2.gunInitialize();
        for (int i = 0; i < 6; i++) {
            roulette2.fireRound();
            if (!roulette2.isShotDeadly()) {
                break; // found a blank on r2
            }
        }
        assertFalse("roulette2 just fired a blank", roulette2.isShotDeadly());
        assertTrue("roulette1 deadly state must NOT be corrupted by roulette2 blank!", roulette1.isShotDeadly());
    }

    @Test
    public void testComputerPlayerHistoryIsolation() {
        Computer_Player comp1 = new Computer_Player();
        Computer_Player comp2 = new Computer_Player();

        comp1.recordPlayerChoice(1);
        comp1.recordPlayerChoice(1);
        comp1.recordPlayerChoice(1);

        assertEquals(3, comp1.getPlayerHistoryCount());
        assertEquals(0, comp2.getPlayerHistoryCount());
    }

    @Test
    public void testGameSessionLifecycle() {
        GameSession session = GameSession.getInstance();
        session.startNewGame(true);

        assertTrue(session.isRussianRoulette());
        assertEquals(1, session.getCurrentRound());
        assertNotNull(session.getPlayer());
        assertNotNull(session.getComputer());
        assertNotNull(session.getRussianRoulette());
        assertEquals(6, session.getRussianRoulette().getTotalRounds());

        session.advanceRound();
        assertEquals(2, session.getCurrentRound());

        session.incrementPlayerScore();
        assertEquals(1, session.getPlayerScore());
    }
}


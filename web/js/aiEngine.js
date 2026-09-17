/**
 * Adaptive AI Engine
 * Predicts player patterns based on Markov frequency & counter-hand tactics
 */

import { HANDS, getWinningCounter, getLosingHand, generateRandomHandPair, evaluateClash, OUTCOMES } from './rpsEngine.js';

export const AI_DIFFICULTY = Object.freeze({
  EASY: 'EASY',
  MEDIUM: 'MEDIUM',
  HARD: 'HARD',
  NIGHTMARISH: 'NIGHTMARISH'
});

export const DIFFICULTY_WEIGHTS = Object.freeze({
  [AI_DIFFICULTY.EASY]: 0.25,
  [AI_DIFFICULTY.MEDIUM]: 0.55,
  [AI_DIFFICULTY.HARD]: 0.80,
  [AI_DIFFICULTY.NIGHTMARISH]: 0.95
});

export class AIEngine {
  constructor(difficulty = AI_DIFFICULTY.HARD) {
    this.difficulty = difficulty;
    this.playerHistory = []; // Array of hand numbers [1, 2, 3...]
    this.frequency = {
      [HANDS.ROCK]: 0,
      [HANDS.PAPER]: 0,
      [HANDS.SCISSORS]: 0
    };
    // Markov transition table: lastHand -> { nextHand: count }
    this.transitions = {
      [HANDS.ROCK]: { [HANDS.ROCK]: 0, [HANDS.PAPER]: 0, [HANDS.SCISSORS]: 0 },
      [HANDS.PAPER]: { [HANDS.ROCK]: 0, [HANDS.PAPER]: 0, [HANDS.SCISSORS]: 0 },
      [HANDS.SCISSORS]: { [HANDS.ROCK]: 0, [HANDS.PAPER]: 0, [HANDS.SCISSORS]: 0 }
    };
  }

  setDifficulty(level) {
    if (AI_DIFFICULTY[level]) {
      this.difficulty = level;
    }
  }

  /**
   * Record player's finalized card pick to adapt future strategies
   */
  recordPlayerChoice(hand) {
    if (!this.frequency[hand] && this.frequency[hand] !== 0) return;

    // Record transition if we have previous pick
    if (this.playerHistory.length > 0) {
      const prev = this.playerHistory[this.playerHistory.length - 1];
      if (this.transitions[prev]) {
        this.transitions[prev][hand] = (this.transitions[prev][hand] || 0) + 1;
      }
    }

    this.playerHistory.push(hand);
    this.frequency[hand]++;
  }

  /**
   * Predict the hand the player is most likely to pick between playerHandA and playerHandB
   */
  predictPlayerChoice(candidateA, candidateB) {
    const candidates = [candidateA, candidateB].filter(Boolean);
    if (candidates.length === 1) return candidates[0];
    if (candidates.length === 0) return HANDS.ROCK;

    // Under low history or random threshold: pick randomly
    const intelligence = DIFFICULTY_WEIGHTS[this.difficulty] || 0.7;
    if (Math.random() > intelligence || this.playerHistory.length < 2) {
      return candidates[Math.floor(Math.random() * candidates.length)];
    }

    // High difficulty: evaluate Markov transition if available
    const lastPick = this.playerHistory[this.playerHistory.length - 1];
    if (this.difficulty === AI_DIFFICULTY.NIGHTMARISH && lastPick && this.transitions[lastPick]) {
      const trans = this.transitions[lastPick];
      const countA = trans[candidateA] || 0;
      const countB = trans[candidateB] || 0;
      if (countA !== countB) {
        return countA > countB ? candidateA : candidateB;
      }
    }

    // Frequency analysis
    const freqA = this.frequency[candidateA] || 0;
    const freqB = this.frequency[candidateB] || 0;
    if (freqA !== freqB) {
      return freqA > freqB ? candidateA : candidateB;
    }

    // Default 50/50 between candidates
    return candidates[Math.floor(Math.random() * candidates.length)];
  }

  /**
   * Generate 2 initial distinct hands for the computer
   */
  generatePair() {
    return generateRandomHandPair();
  }

  /**
   * Select final hand from computer's 2 cards based on player's visible cards
   */
  chooseFinalHand(compA, compB, playerA, playerB) {
    const predictedPlayerHand = this.predictPlayerChoice(playerA, playerB);

    // Check if compA or compB beats the predicted hand
    const compAClash = evaluateClash(compA, predictedPlayerHand);
    const compBClash = evaluateClash(compB, predictedPlayerHand);

    // If compA wins against prediction, choose compA
    if (compAClash === OUTCOMES.WIN && compBClash !== OUTCOMES.WIN) {
      return compA;
    }
    // If compB wins against prediction, choose compB
    if (compBClash === OUTCOMES.WIN && compAClash !== OUTCOMES.WIN) {
      return compB;
    }

    // If both or neither win, prefer a tie over an outright loss
    if (compAClash === OUTCOMES.TIE && compBClash === OUTCOMES.LOSE) {
      return compA;
    }
    if (compBClash === OUTCOMES.TIE && compAClash === OUTCOMES.LOSE) {
      return compB;
    }

    // Otherwise random 50/50 between computer's two choices
    return Math.random() < 0.5 ? compA : compB;
  }

  /**
   * Telemetry data for Operator/Dev Mode HUD
   */
  getTelemetry() {
    const total = this.playerHistory.length || 1;
    return {
      difficulty: this.difficulty,
      intelligenceRate: `${Math.round((DIFFICULTY_WEIGHTS[this.difficulty] || 0.7) * 100)}%`,
      totalPlayerRounds: this.playerHistory.length,
      distribution: {
        rock: {
          count: this.frequency[HANDS.ROCK],
          percentage: Math.round((this.frequency[HANDS.ROCK] / total) * 100)
        },
        paper: {
          count: this.frequency[HANDS.PAPER],
          percentage: Math.round((this.frequency[HANDS.PAPER] / total) * 100)
        },
        scissors: {
          count: this.frequency[HANDS.SCISSORS],
          percentage: Math.round((this.frequency[HANDS.SCISSORS] / total) * 100)
        }
      },
      lastFivePlayerPicks: this.playerHistory.slice(-5)
    };
  }

  reset() {
    this.playerHistory = [];
    this.frequency = { [HANDS.ROCK]: 0, [HANDS.PAPER]: 0, [HANDS.SCISSORS]: 0 };
    for (const key of Object.keys(this.transitions)) {
      this.transitions[key] = { [HANDS.ROCK]: 0, [HANDS.PAPER]: 0, [HANDS.SCISSORS]: 0 };
    }
  }
}

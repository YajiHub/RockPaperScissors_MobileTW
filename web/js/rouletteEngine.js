/**
 * Russian Roulette Cylinder Engine
 * Handles chamber state, bullet distribution, and survival odds calculations
 */

export class RouletteEngine {
  constructor(capacity = 6, bulletCount = 1) {
    this.totalChambers = capacity;
    this.bulletCount = Math.min(bulletCount, capacity - 1);
    this.currentRound = 0; // 0-indexed
    this.chambers = [];
    this.history = [];
    this.initialize();
  }

  /**
   * Initialize a new cylinder spin and bullet loading
   */
  initialize(capacity = this.totalChambers, bulletCount = this.bulletCount) {
    this.totalChambers = capacity;
    this.bulletCount = Math.min(bulletCount, capacity - 1);
    this.currentRound = 0;
    this.chambers = new Array(this.totalChambers).fill(0);
    this.history = [];

    // Place live bullet(s) at random distinct indices
    const available = Array.from({ length: this.totalChambers }, (_, i) => i);
    for (let b = 0; b < this.bulletCount; b++) {
      const pickIdx = Math.floor(Math.random() * available.length);
      const chamberIdx = available.splice(pickIdx, 1)[0];
      this.chambers[chamberIdx] = 1;
    }

    return this.getState();
  }

  /**
   * Calculate live bullets remaining in un-fired chambers
   */
  getRemainingLiveBullets() {
    let count = 0;
    for (let i = this.currentRound; i < this.totalChambers; i++) {
      if (this.chambers[i] === 1) count++;
    }
    return count;
  }

  /**
   * Remaining un-fired chamber count
   */
  getRemainingChambers() {
    return Math.max(0, this.totalChambers - this.currentRound);
  }

  /**
   * Current probability of terminal shot on the next pull (0.0 to 1.0)
   */
  getCurrentDeathProbability() {
    const remaining = this.getRemainingChambers();
    if (remaining <= 0) return 0;
    const remainingBullets = this.getRemainingLiveBullets();
    return remainingBullets / remaining;
  }

  /**
   * Formatted percentage string
   */
  getFormattedOdds() {
    const prob = this.getCurrentDeathProbability();
    return (prob * 100).toFixed(1) + '%';
  }

  /**
   * Pull the trigger on the current chamber
   * @param {'PLAYER' | 'COMPUTER'} target - Who the barrel is pointed at
   * @returns {Object} result of the trigger pull
   */
  pullTrigger(target = 'PLAYER') {
    if (this.currentRound >= this.totalChambers) {
      return {
        isLive: false,
        exhausted: true,
        chamber: this.currentRound,
        target
      };
    }

    const isLive = this.chambers[this.currentRound] === 1;
    const roundNumber = this.currentRound + 1;

    this.history.push({
      round: roundNumber,
      target,
      isLive,
      odds: this.getFormattedOdds()
    });

    this.currentRound++;

    return {
      isLive,
      exhausted: this.currentRound >= this.totalChambers,
      round: roundNumber,
      totalChambers: this.totalChambers,
      target,
      nextOdds: this.getFormattedOdds()
    };
  }

  /**
   * Force set a specific chamber as live for Operator testing
   */
  forceSetBullet(chamberIndex) {
    if (chamberIndex >= 0 && chamberIndex < this.totalChambers) {
      this.chambers.fill(0);
      this.chambers[chamberIndex] = 1;
      this.bulletCount = 1;
    }
  }

  getState() {
    return {
      currentRound: this.currentRound,
      totalChambers: this.totalChambers,
      bulletCount: this.bulletCount,
      chambers: [...this.chambers],
      remainingChambers: this.getRemainingChambers(),
      remainingLive: this.getRemainingLiveBullets(),
      deathProbability: this.getCurrentDeathProbability(),
      formattedOdds: this.getFormattedOdds(),
      history: [...this.history]
    };
  }
}

/**
 * Central State Machine
 * Coordinates game phases, active players, and view navigation
 */

export const SCREENS = Object.freeze({
  MENU: 'MENU',
  MODE_SELECT: 'MODE_SELECT',
  CYLINDER_INIT: 'CYLINDER_INIT',
  PICK_TWO_HANDS: 'PICK_TWO_HANDS',
  REVEAL_HANDS: 'REVEAL_HANDS',
  FINAL_PICK: 'FINAL_PICK',
  CLASH_RESOLUTION: 'CLASH_RESOLUTION',
  TRIGGER_PULL: 'TRIGGER_PULL',
  GAME_OVER: 'GAME_OVER'
});

export const GAME_MODES = Object.freeze({
  NORMAL: 'NORMAL',
  RUSSIAN: 'RUSSIAN'
});

export class GameState {
  constructor() {
    this.currentScreen = SCREENS.MENU;
    this.gameMode = GAME_MODES.NORMAL;

    // Current Round State
    this.playerHands = [null, null]; // [hand1, hand2]
    this.computerHands = [null, null];
    this.playerFinalHand = null;
    this.computerFinalHand = null;
    this.roundOutcome = null; // 'WIN' | 'LOSE' | 'TIE'

    // Score / Progression
    this.playerScore = 0;
    this.computerScore = 0;
    this.currentRoundNumber = 1;

    // Trigger target for Russian roulette
    this.triggerTarget = null; // 'PLAYER' | 'COMPUTER'
    this.lastTriggerResult = null;

    // Event listeners
    this.listeners = [];
  }

  subscribe(callback) {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter(cb => cb !== callback);
    };
  }

  notify(event, payload = {}) {
    for (const listener of this.listeners) {
      listener(this.currentScreen, event, payload, this);
    }
  }

  setScreen(newScreen, payload = {}) {
    this.currentScreen = newScreen;
    this.notify('SCREEN_CHANGE', payload);
  }

  setGameMode(mode) {
    this.gameMode = mode;
    this.playerScore = 0;
    this.computerScore = 0;
    this.currentRoundNumber = 1;
    this.resetRound();
    this.notify('MODE_CHANGE', { mode });
  }

  resetRound() {
    this.playerHands = [null, null];
    this.computerHands = [null, null];
    this.playerFinalHand = null;
    this.computerFinalHand = null;
    this.roundOutcome = null;
    this.triggerTarget = null;
    this.lastTriggerResult = null;
  }
}

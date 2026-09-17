/**
 * Two-Hand Rock-Paper-Scissors Engine
 * Handles move definitions, validation, and match outcomes
 */

export const HANDS = Object.freeze({
  ROCK: 1,
  PAPER: 2,
  SCISSORS: 3
});

export const HAND_DETAILS = Object.freeze({
  [HANDS.ROCK]: { name: 'Rock', symbol: '🪨', image: 'assets/images/rock.jpg' },
  [HANDS.PAPER]: { name: 'Paper', symbol: '📄', image: 'assets/images/paper.png' },
  [HANDS.SCISSORS]: { name: 'Scissors', symbol: '✂️', image: 'assets/images/scissor.png' }
});

export const OUTCOMES = Object.freeze({
  WIN: 'WIN',
  LOSE: 'LOSE',
  TIE: 'TIE'
});

/**
 * Validate that two hands are distinct and valid
 */
export function isValidTwoHandSelection(handA, handB) {
  if (!handA || !handB) return false;
  if (handA === handB) return false;
  const valid = [HANDS.ROCK, HANDS.PAPER, HANDS.SCISSORS];
  return valid.includes(handA) && valid.includes(handB);
}

/**
 * Returns a random distinct pair of hands
 */
export function generateRandomHandPair() {
  const choices = [HANDS.ROCK, HANDS.PAPER, HANDS.SCISSORS];
  const firstIndex = Math.floor(Math.random() * choices.length);
  const hand1 = choices[firstIndex];
  choices.splice(firstIndex, 1);
  const hand2 = choices[Math.floor(Math.random() * choices.length)];
  return [hand1, hand2];
}

/**
 * Evaluates clash between two final hands
 * @returns {string} OUTCOMES.WIN | OUTCOMES.LOSE | OUTCOMES.TIE (from perspective of handA)
 */
export function evaluateClash(handA, handB) {
  if (handA === handB) return OUTCOMES.TIE;

  if (
    (handA === HANDS.ROCK && handB === HANDS.SCISSORS) ||
    (handA === HANDS.PAPER && handB === HANDS.ROCK) ||
    (handA === HANDS.SCISSORS && handB === HANDS.PAPER)
  ) {
    return OUTCOMES.WIN;
  }

  return OUTCOMES.LOSE;
}

/**
 * Returns the winning counter-hand
 */
export function getWinningCounter(hand) {
  switch (hand) {
    case HANDS.ROCK: return HANDS.PAPER;
    case HANDS.PAPER: return HANDS.SCISSORS;
    case HANDS.SCISSORS: return HANDS.ROCK;
    default: return null;
  }
}

/**
 * Returns the losing hand against given hand
 */
export function getLosingHand(hand) {
  switch (hand) {
    case HANDS.ROCK: return HANDS.SCISSORS;
    case HANDS.PAPER: return HANDS.ROCK;
    case HANDS.SCISSORS: return HANDS.PAPER;
    default: return null;
  }
}

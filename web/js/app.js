/**
 * Main Application Controller
 * Orchestrates Two-Hand RPS, Russian Roulette loop, UI states, and audio
 */

import { HANDS, HAND_DETAILS, OUTCOMES, evaluateClash } from './rpsEngine.js';
import { RouletteEngine } from './rouletteEngine.js';
import { AIEngine, AI_DIFFICULTY } from './aiEngine.js';
import { AudioEngine } from './audioEngine.js';
import { StatsEngine } from './statsEngine.js';
import { GameState, SCREENS, GAME_MODES } from './state.js';
import { OperatorHUD } from './operatorHUD.js';

class AppController {
  constructor() {
    this.audio = new AudioEngine();
    this.stats = new StatsEngine();
    this.roulette = new RouletteEngine(6, 1);
    this.ai = new AIEngine(AI_DIFFICULTY.HARD);
    this.state = new GameState();

    this.timerInterval = null;
    this.selectedCards = new Set(); // For Phase 1 selection

    this.initDOM();
    this.bindEvents();
    this.operatorHUD = new OperatorHUD(this.roulette, this.ai, this.audio, this.state);

    // Subscribe to state machine changes
    this.state.subscribe((screen, event, payload) => {
      this.handleStateChange(screen, event, payload);
    });

    this.renderStatsModal();
  }

  initDOM() {
    // Screens
    this.screens = {
      [SCREENS.MENU]: document.getElementById('screen-menu'),
      [SCREENS.MODE_SELECT]: document.getElementById('screen-mode-select'),
      [SCREENS.CYLINDER_INIT]: document.getElementById('screen-cylinder-init'),
      [SCREENS.PICK_TWO_HANDS]: document.getElementById('screen-pick-two'),
      [SCREENS.REVEAL_HANDS]: document.getElementById('screen-reveal'),
      [SCREENS.FINAL_PICK]: document.getElementById('screen-final-pick'),
      [SCREENS.CLASH_RESOLUTION]: document.getElementById('screen-clash'),
      [SCREENS.TRIGGER_PULL]: document.getElementById('screen-trigger'),
      [SCREENS.GAME_OVER]: document.getElementById('screen-game-over')
    };

    // Global HUD elements
    this.hudMode = document.getElementById('hud-mode-name');
    this.hudRound = document.getElementById('hud-round-number');
    this.hudOddsContainer = document.getElementById('hud-odds-container');
    this.hudOddsValue = document.getElementById('hud-odds-value');
    this.hudScore = document.getElementById('hud-score-display');

    // Cylinder anim image
    this.cylinderImg = document.getElementById('cylinder-anim-frame');
    this.cylinderStatus = document.getElementById('cylinder-status-text');

    // Confirm button for Phase 1
    this.btnConfirmTwo = document.getElementById('btn-confirm-two-hands');

    // Modals
    this.modalStats = document.getElementById('modal-stats');
    this.modalSettings = document.getElementById('modal-settings');
    this.modalEmbed = document.getElementById('modal-embed');

    // Settings inputs
    this.volumeSlider = document.getElementById('setting-volume-slider');
    this.aiDifficultySelect = document.getElementById('setting-ai-difficulty');
    this.hapticsToggle = document.getElementById('setting-haptics-toggle');
    this.timerToggle = document.getElementById('setting-timer-toggle');

    // Flash overlay & container
    this.flashOverlay = document.getElementById('flash-overlay');
    this.appContainer = document.getElementById('app-container');
  }

  bindEvents() {
    // Global Header Nav Buttons
    document.getElementById('btn-sound-toggle')?.addEventListener('click', () => {
      const isMuted = this.audio.toggleMute();
      const btn = document.getElementById('btn-sound-toggle');
      if (btn) btn.textContent = isMuted ? '🔇' : '🔊';
    });

    document.getElementById('btn-open-stats')?.addEventListener('click', () => {
      this.renderStatsModal();
      this.openModal(this.modalStats);
      this.audio.playButtonClick();
    });

    document.getElementById('btn-open-settings')?.addEventListener('click', () => {
      this.openModal(this.modalSettings);
      this.audio.playButtonClick();
    });

    document.getElementById('btn-open-embed')?.addEventListener('click', () => {
      this.openModal(this.modalEmbed);
      this.audio.playButtonClick();
    });

    // Close Modal buttons
    document.querySelectorAll('.modal-close').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const targetId = e.target.getAttribute('data-close');
        const modal = document.getElementById(targetId);
        if (modal) this.closeModal(modal);
      });
    });

    // Reset stats button
    document.getElementById('btn-reset-stats')?.addEventListener('click', () => {
      if (confirm('Reset all game records and win streaks?')) {
        this.stats.reset();
        this.renderStatsModal();
        this.audio.playButtonClick();
      }
    });

    // Copy embed code snippet
    document.getElementById('btn-copy-embed')?.addEventListener('click', () => {
      const code = document.querySelector('.code-box')?.textContent || '';
      navigator.clipboard.writeText(code).then(() => {
        const btn = document.getElementById('btn-copy-embed');
        if (btn) {
          const original = btn.textContent;
          btn.textContent = '✅ Copied to Clipboard!';
          setTimeout(() => { btn.textContent = original; }, 2000);
        }
      });
    });

    // Settings Listeners
    this.volumeSlider?.addEventListener('input', (e) => {
      this.audio.setVolume(parseFloat(e.target.value));
    });

    this.aiDifficultySelect?.addEventListener('change', (e) => {
      this.ai.setDifficulty(e.target.value);
    });

    this.hapticsToggle?.addEventListener('change', (e) => {
      this.audio.hapticsEnabled = e.target.checked;
    });

    // Menu Navigation
    document.getElementById('btn-menu-play')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setScreen(SCREENS.MODE_SELECT);
    });

    document.getElementById('btn-menu-stats')?.addEventListener('click', () => {
      this.renderStatsModal();
      this.openModal(this.modalStats);
      this.audio.playButtonClick();
    });

    document.getElementById('btn-menu-settings')?.addEventListener('click', () => {
      this.openModal(this.modalSettings);
      this.audio.playButtonClick();
    });

    document.getElementById('btn-back-to-menu')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setScreen(SCREENS.MENU);
    });

    // Mode Selection Cards
    document.getElementById('mode-normal-card')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setGameMode(GAME_MODES.NORMAL);
      this.startRound();
    });

    document.getElementById('mode-russian-card')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setGameMode(GAME_MODES.RUSSIAN);
      this.roulette.initialize(6, 1);
      this.startCylinderLoadingSequence();
    });

    // Phase 1 Card Selection
    document.querySelectorAll('#screen-pick-two .game-card').forEach(card => {
      card.addEventListener('click', () => {
        const hand = parseInt(card.getAttribute('data-hand'), 10);
        this.toggleHandSelection(hand, card);
      });
    });

    this.btnConfirmTwo?.addEventListener('click', () => {
      if (this.selectedCards.size === 2) {
        this.confirmTwoHands();
      }
    });

    // Phase 2 Proceed to Final Pick
    document.getElementById('btn-proceed-final-pick')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setScreen(SCREENS.FINAL_PICK);
    });

    // Phase 4 Clash Continue Button
    document.getElementById('btn-clash-continue')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.handleClashContinue();
    });

    // Trigger Pull Button
    document.getElementById('btn-pull-trigger')?.addEventListener('click', () => {
      this.handleTriggerPull();
    });

    // Game Over Buttons
    document.getElementById('btn-play-again')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      if (this.state.gameMode === GAME_MODES.RUSSIAN) {
        this.roulette.initialize(6, 1);
        this.startCylinderLoadingSequence();
      } else {
        this.startRound();
      }
    });

    document.getElementById('btn-game-over-menu')?.addEventListener('click', () => {
      this.audio.playButtonClick();
      this.state.setScreen(SCREENS.MENU);
    });
  }

  openModal(modal) {
    if (modal) modal.classList.add('active');
  }

  closeModal(modal) {
    if (modal) modal.classList.remove('active');
  }

  renderStatsModal() {
    const summary = this.stats.getSummary();
    const update = (id, val) => {
      const el = document.getElementById(id);
      if (el) el.textContent = val;
    };

    update('stat-total-games', summary.totalGames);
    update('stat-win-rate', summary.winRate);
    update('stat-normal-wins', summary.normalWins);
    update('stat-normal-losses', summary.normalLosses);
    update('stat-russian-wins', summary.russianWins);
    update('stat-russian-deaths', summary.russianDeaths);
    update('stat-best-streak', summary.bestStreak);
    update('stat-survival-rate', summary.survivalRate);
  }

  handleStateChange(screen) {
    // Hide all screens, show active
    for (const [key, el] of Object.entries(this.screens)) {
      if (el) {
        el.classList.toggle('active', key === screen);
      }
    }

    // Update Operator HUD whenever screen changes
    this.operatorHUD?.render();

    // Screen-specific setup
    if (screen === SCREENS.PICK_TWO_HANDS) {
      this.setupPickTwoHandsScreen();
    } else if (screen === SCREENS.REVEAL_HANDS) {
      this.setupRevealScreen();
    } else if (screen === SCREENS.FINAL_PICK) {
      this.setupFinalPickScreen();
    } else if (screen === SCREENS.CLASH_RESOLUTION) {
      this.setupClashScreen();
    } else if (screen === SCREENS.TRIGGER_PULL) {
      this.setupTriggerScreen();
    } else if (screen === SCREENS.GAME_OVER) {
      this.setupGameOverScreen();
    }
  }

  // ==========================================================================
  // Russian Roulette Loading Sequence
  // ==========================================================================
  startCylinderLoadingSequence() {
    this.state.setScreen(SCREENS.CYLINDER_INIT);
    this.audio.playGunLoad();

    let frame = 1;
    let loops = 0;
    const maxLoops = 22; // Spin animation frames duration

    const interval = setInterval(() => {
      frame = (frame % 10) + 1;
      const frameStr = frame < 10 ? `0${frame}` : `${frame}`;
      if (this.cylinderImg) {
        this.cylinderImg.src = `assets/images/frame${frameStr}.png`;
      }

      loops++;
      if (loops === 5) {
        this.audio.playGunSpin();
        if (this.cylinderStatus) this.cylinderStatus.textContent = 'SPINNING CYLINDER...';
      }

      if (loops >= maxLoops) {
        clearInterval(interval);
        this.audio.playGunCock();
        if (this.cylinderStatus) this.cylinderStatus.textContent = 'CHAMBER LOCKED & READY.';
        setTimeout(() => {
          this.startRound();
        }, 700);
      }
    }, 75);
  }

  startRound() {
    this.state.resetRound();
    this.selectedCards.clear();
    this.state.setScreen(SCREENS.PICK_TWO_HANDS);
  }

  // ==========================================================================
  // Phase 1: Pick Two Hands
  // ==========================================================================
  setupPickTwoHandsScreen() {
    // Update HUD
    const isRussian = this.state.gameMode === GAME_MODES.RUSSIAN;
    if (this.hudMode) {
      this.hudMode.textContent = isRussian ? 'RUSSIAN ROULETTE' : 'NORMAL';
      this.hudMode.style.color = isRussian ? 'var(--accent-crimson)' : 'var(--accent-cyan)';
    }
    if (this.hudRound) this.hudRound.textContent = this.state.currentRoundNumber;
    if (this.hudScore) this.hudScore.textContent = `${this.state.playerScore} - ${this.state.computerScore}`;

    if (this.hudOddsContainer) {
      this.hudOddsContainer.style.display = isRussian ? 'flex' : 'none';
      if (this.hudOddsValue) {
        this.hudOddsValue.textContent = this.roulette.getFormattedOdds();
      }
    }

    // Reset card UI selections
    document.querySelectorAll('#screen-pick-two .game-card').forEach(card => {
      card.classList.remove('selected', 'disabled');
    });

    if (this.btnConfirmTwo) {
      this.btnConfirmTwo.style.opacity = '0.5';
      this.btnConfirmTwo.style.pointerEvents = 'none';
      this.btnConfirmTwo.textContent = 'LOCK IN HANDS (0/2)';
    }
  }

  toggleHandSelection(hand, cardElement) {
    if (this.selectedCards.has(hand)) {
      this.selectedCards.delete(hand);
      cardElement.classList.remove('selected');
      this.audio.playCardSelect();
    } else {
      if (this.selectedCards.size < 2) {
        this.selectedCards.add(hand);
        cardElement.classList.add('selected');
        this.audio.playCardSelect();
      }
    }

    const count = this.selectedCards.size;
    if (this.btnConfirmTwo) {
      this.btnConfirmTwo.textContent = `LOCK IN HANDS (${count}/2)`;
      if (count === 2) {
        this.btnConfirmTwo.style.opacity = '1';
        this.btnConfirmTwo.style.pointerEvents = 'auto';
      } else {
        this.btnConfirmTwo.style.opacity = '0.5';
        this.btnConfirmTwo.style.pointerEvents = 'none';
      }
    }
  }

  confirmTwoHands() {
    const handsArray = Array.from(this.selectedCards);
    this.state.playerHands = handsArray;

    // Computer generates pair
    this.state.computerHands = this.ai.generatePair();

    this.audio.playButtonClick();
    this.state.setScreen(SCREENS.REVEAL_HANDS);
  }

  // ==========================================================================
  // Phase 2: Reveal Both Pairs
  // ==========================================================================
  setupRevealScreen() {
    const playerContainer = document.getElementById('player-reveal-pair');
    const enemyContainer = document.getElementById('enemy-reveal-pair');

    if (playerContainer) playerContainer.innerHTML = '';
    if (enemyContainer) enemyContainer.innerHTML = '';

    // Render Player's 2 cards
    this.state.playerHands.forEach(handNum => {
      const card = this.createCardElement(handNum);
      playerContainer?.appendChild(card);
    });

    // Render Enemy's 2 cards
    this.state.computerHands.forEach(handNum => {
      const card = this.createCardElement(handNum);
      enemyContainer?.appendChild(card);
    });

    this.audio.playGunCock();
  }

  createCardElement(handNumber, isClickable = false, onClick = null) {
    const details = HAND_DETAILS[handNumber];
    const card = document.createElement('div');
    card.className = 'game-card anim-deal';
    card.innerHTML = `
      <span class="card-name">${details.name.toUpperCase()}</span>
      <img class="card-img" src="${details.image}" alt="${details.name}">
      <span style="font-size: 0.75rem; color: var(--text-muted);">${details.symbol}</span>
    `;

    if (isClickable && onClick) {
      card.style.cursor = 'pointer';
      card.addEventListener('click', onClick);
    } else {
      card.style.cursor = 'default';
    }

    return card;
  }

  // ==========================================================================
  // Phase 3: Final Pick
  // ==========================================================================
  setupFinalPickScreen() {
    const optionsContainer = document.getElementById('player-final-options');
    if (optionsContainer) optionsContainer.innerHTML = '';

    // Render player's 2 cards as clickable final choices
    this.state.playerHands.forEach(handNum => {
      const card = this.createCardElement(handNum, true, () => {
        this.selectFinalHand(handNum);
      });
      optionsContainer?.appendChild(card);
    });

    // Setup optional countdown timer
    const timerFill = document.getElementById('final-pick-timer-fill');
    clearInterval(this.timerInterval);

    const timerEnabled = this.timerToggle ? this.timerToggle.checked : true;
    if (timerEnabled && timerFill) {
      timerFill.style.width = '100%';
      const durationMs = 5000;
      const step = 50;
      let elapsed = 0;

      this.timerInterval = setInterval(() => {
        elapsed += step;
        const pct = Math.max(0, 100 - (elapsed / durationMs) * 100);
        timerFill.style.width = `${pct}%`;

        if (elapsed >= durationMs) {
          clearInterval(this.timerInterval);
          // Auto-select first available hand
          if (!this.state.playerFinalHand) {
            this.selectFinalHand(this.state.playerHands[0]);
          }
        }
      }, step);
    }
  }

  selectFinalHand(handNum) {
    clearInterval(this.timerInterval);
    this.state.playerFinalHand = handNum;

    // AI selects final hand from its 2 cards
    const compFinal = this.ai.chooseFinalHand(
      this.state.computerHands[0],
      this.state.computerHands[1],
      this.state.playerHands[0],
      this.state.playerHands[1]
    );
    this.state.computerFinalHand = compFinal;

    // Record player choice in AI history
    this.ai.recordPlayerChoice(handNum);

    this.audio.playCardSelect();
    this.state.setScreen(SCREENS.CLASH_RESOLUTION);
  }

  // ==========================================================================
  // Phase 4: Clash Resolution
  // ==========================================================================
  setupClashScreen() {
    const playerCardContainer = document.getElementById('clash-player-card');
    const enemyCardContainer = document.getElementById('clash-enemy-card');
    const outcomeBanner = document.getElementById('clash-outcome-banner');

    const playerHand = this.state.playerFinalHand;
    const compHand = this.state.computerFinalHand;

    const pDetails = HAND_DETAILS[playerHand];
    const cDetails = HAND_DETAILS[compHand];

    if (playerCardContainer) {
      playerCardContainer.innerHTML = `
        <span class="card-name">${pDetails.name.toUpperCase()}</span>
        <img class="card-img" src="${pDetails.image}" alt="${pDetails.name}">
        <span style="font-size: 0.8rem;">${pDetails.symbol}</span>
      `;
      playerCardContainer.classList.add('anim-deal');
    }

    if (enemyCardContainer) {
      enemyCardContainer.innerHTML = `
        <span class="card-name">${cDetails.name.toUpperCase()}</span>
        <img class="card-img" src="${cDetails.image}" alt="${cDetails.name}">
        <span style="font-size: 0.8rem;">${cDetails.symbol}</span>
      `;
      enemyCardContainer.classList.add('anim-deal');
    }

    // Evaluate Clash
    const result = evaluateClash(playerHand, compHand);
    this.state.roundOutcome = result;

    if (outcomeBanner) {
      if (result === OUTCOMES.WIN) {
        outcomeBanner.textContent = '🎉 YOU WIN THE CLASH!';
        outcomeBanner.style.color = 'var(--accent-green)';
        this.audio.playWin();
        this.state.playerScore++;
      } else if (result === OUTCOMES.LOSE) {
        outcomeBanner.textContent = '💀 DEFEAT! OPPONENT WINS!';
        outcomeBanner.style.color = 'var(--accent-crimson)';
        this.audio.playLose();
        this.state.computerScore++;
      } else {
        outcomeBanner.textContent = '⚖️ DRAW! BOTH SIDES CLASH TIED!';
        outcomeBanner.style.color = 'var(--accent-gold)';
        this.audio.playTie();
      }
    }
  }

  handleClashContinue() {
    if (this.state.gameMode === GAME_MODES.NORMAL) {
      // In Normal Mode: check if best of 5 (3 wins) reached, or proceed next round
      this.stats.recordNormalResult(this.state.roundOutcome);
      this.renderStatsModal();

      if (this.state.playerScore >= 3 || this.state.computerScore >= 3) {
        this.state.setScreen(SCREENS.GAME_OVER);
      } else {
        this.state.currentRoundNumber++;
        this.startRound();
      }
    } else {
      // Russian Roulette Mode:
      if (this.state.roundOutcome === OUTCOMES.TIE) {
        // Tie: Re-play round without trigger pull
        this.startRound();
      } else {
        // Loser faces trigger pull!
        this.state.triggerTarget = this.state.roundOutcome === OUTCOMES.WIN ? 'COMPUTER' : 'PLAYER';
        this.state.setScreen(SCREENS.TRIGGER_PULL);
      }
    }
  }

  // ==========================================================================
  // Screen 8: Russian Roulette Trigger Pull
  // ==========================================================================
  setupTriggerScreen() {
    const isPlayer = this.state.triggerTarget === 'PLAYER';
    const chamberNum = document.getElementById('trigger-chamber-num');
    const headline = document.getElementById('trigger-headline');
    const subtext = document.getElementById('trigger-subtext');
    const oddsBadge = document.getElementById('trigger-odds-badge');
    const gunImg = document.getElementById('trigger-gun-img');
    const resultMsg = document.getElementById('trigger-result-msg');
    const pullBtn = document.getElementById('btn-pull-trigger');

    if (chamberNum) chamberNum.textContent = this.roulette.currentRound + 1;
    if (oddsBadge) oddsBadge.textContent = this.roulette.getFormattedOdds();
    if (gunImg) gunImg.src = 'assets/images/gun.png';
    if (resultMsg) resultMsg.textContent = '';
    if (pullBtn) {
      pullBtn.style.display = 'inline-block';
      pullBtn.disabled = false;
      pullBtn.textContent = isPlayer ? 'PULL TRIGGER 💥' : 'FORCE ENEMY TRIGGER 💥';
    }

    if (headline) {
      headline.textContent = isPlayer ? 'BARREL POINTED AT YOU' : 'BARREL POINTED AT ENEMY';
      headline.style.color = isPlayer ? 'var(--accent-crimson)' : 'var(--accent-cyan)';
    }

    if (subtext) {
      subtext.innerHTML = isPlayer
        ? `You lost the clash. Next chamber: <strong>${this.roulette.getFormattedOdds()}</strong> chance of fatal blast.`
        : `Enemy lost the clash. Next chamber: <strong>${this.roulette.getFormattedOdds()}</strong> chance of fatal blast.`;
    }

    this.audio.playGunCock();
  }

  handleTriggerPull() {
    const pullBtn = document.getElementById('btn-pull-trigger');
    const gunImg = document.getElementById('trigger-gun-img');
    const resultMsg = document.getElementById('trigger-result-msg');
    if (pullBtn) pullBtn.disabled = true;

    const pullResult = this.roulette.pullTrigger(this.state.triggerTarget);
    this.operatorHUD?.render();

    if (pullResult.isLive) {
      // Live Bullet Fired!
      if (gunImg) gunImg.src = 'assets/images/firing_revolver_gun.png';
      this.audio.playLiveShot();

      // Screen flash & shake
      this.flashOverlay?.classList.remove('flash-active');
      this.appContainer?.classList.remove('anim-shake');
      void this.flashOverlay?.offsetWidth;
      this.flashOverlay?.classList.add('flash-active');
      this.appContainer?.classList.add('anim-shake');

      const isPlayerVictim = this.state.triggerTarget === 'PLAYER';
      if (resultMsg) {
        resultMsg.textContent = `💥 BOOM! The live round discharged at the ${this.state.triggerTarget}!`;
        resultMsg.style.color = 'var(--accent-crimson)';
      }

      // Record Stats
      this.stats.recordRussianResult(!isPlayerVictim, isPlayerVictim, this.roulette.currentRound);
      this.renderStatsModal();

      setTimeout(() => {
        this.state.setScreen(SCREENS.GAME_OVER);
      }, 1600);
    } else {
      // Blank Click!
      this.audio.playBlankShot();
      this.appContainer?.classList.remove('anim-blank');
      void this.appContainer?.offsetWidth;
      this.appContainer?.classList.add('anim-blank');

      if (resultMsg) {
        resultMsg.textContent = `*CLICK!* Chamber #${pullResult.round} was empty. Survival confirmed!`;
        resultMsg.style.color = 'var(--accent-green)';
      }

      setTimeout(() => {
        if (pullResult.exhausted) {
          // Cylinder emptied without death, reload
          this.roulette.initialize(6, 1);
          this.startCylinderLoadingSequence();
        } else {
          this.state.currentRoundNumber++;
          this.startRound();
        }
      }, 1400);
    }
  }

  // ==========================================================================
  // Screen 9: Game Over
  // ==========================================================================
  setupGameOverScreen() {
    const title = document.getElementById('game-over-title');
    const subtitle = document.getElementById('game-over-subtitle');
    const statRounds = document.getElementById('go-stat-rounds');
    const statStreak = document.getElementById('go-stat-streak');

    const isRussian = this.state.gameMode === GAME_MODES.RUSSIAN;
    let didPlayerWin = false;

    if (isRussian) {
      didPlayerWin = this.state.triggerTarget === 'COMPUTER';
    } else {
      didPlayerWin = this.state.playerScore > this.state.computerScore;
    }

    if (title) {
      title.textContent = didPlayerWin ? 'VICTORY' : 'DEFEAT';
      title.style.color = didPlayerWin ? 'var(--accent-gold)' : 'var(--accent-crimson)';
    }

    if (subtitle) {
      if (isRussian) {
        subtitle.textContent = didPlayerWin
          ? 'The enemy met their fate. You walked away alive from the roulette.'
          : 'The fatal chamber caught you. Better luck in the next life.';
      } else {
        subtitle.textContent = didPlayerWin
          ? 'You triumphed over the AI with superior tactical hand predictions.'
          : 'The AI counter-play anticipated your decisions. Re-evaluate your strategy.';
      }
    }

    if (statRounds) {
      statRounds.textContent = isRussian ? this.roulette.currentRound : this.state.currentRoundNumber;
    }
    if (statStreak) {
      statStreak.textContent = this.stats.getSummary().currentStreak;
    }

    if (didPlayerWin) {
      this.audio.playWin();
    } else {
      this.audio.playLose();
    }
  }
}

// Instantiate and initialize on DOM ready
window.addEventListener('DOMContentLoaded', () => {
  window.app = new AppController();
});

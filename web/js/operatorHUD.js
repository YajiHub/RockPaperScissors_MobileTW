/**
 * Recruiter & Operator Dev Mode HUD
 * Provides real-time observability into cylinder state, Markov telemetry, and balance controls
 */

export class OperatorHUD {
  constructor(rouletteEngine, aiEngine, audioEngine, gameState) {
    this.roulette = rouletteEngine;
    this.ai = aiEngine;
    this.audio = audioEngine;
    this.state = gameState;

    this.drawer = document.getElementById('operator-drawer');
    this.toggleBtn = document.getElementById('btn-toggle-operator');
    this.closeBtn = document.getElementById('btn-close-operator');

    this.cylinderContainer = document.getElementById('op-cylinder-visualizer');
    this.liveOddsText = document.getElementById('op-live-odds');
    this.bulletChamberText = document.getElementById('op-bullet-chamber');
    this.cylinderSummaryText = document.getElementById('op-cylinder-summary');

    this.rockFill = document.getElementById('bar-rock-fill');
    this.paperFill = document.getElementById('bar-paper-fill');
    this.scissorsFill = document.getElementById('bar-scissors-fill');
    this.rockPct = document.getElementById('label-rock-pct');
    this.paperPct = document.getElementById('label-paper-pct');
    this.scissorsPct = document.getElementById('label-scissors-pct');
    this.predictedPickText = document.getElementById('op-ai-predicted-pick');
    this.aiIqText = document.getElementById('op-ai-iq-rate');

    this.capacitySelect = document.getElementById('op-chamber-capacity');
    this.forceBlankBtn = document.getElementById('op-btn-force-blank');
    this.forceLiveBtn = document.getElementById('op-btn-force-live');
    this.testBlankBtn = document.getElementById('op-test-blank');
    this.testLiveBtn = document.getElementById('op-test-live');

    this.bindEvents();
    this.render();
  }

  bindEvents() {
    this.toggleBtn?.addEventListener('click', () => this.toggle());
    this.closeBtn?.addEventListener('click', () => this.close());

    // Keyboard shortcut (Tilde ` or Escape)
    window.addEventListener('keydown', (e) => {
      if (e.key === '`' || e.key === '~') {
        this.toggle();
      } else if (e.key === 'Escape' && this.isOpen()) {
        this.close();
      }
    });

    // Chamber capacity change
    this.capacitySelect?.addEventListener('change', (e) => {
      const newCap = parseInt(e.target.value, 10);
      this.roulette.initialize(newCap, 1);
      this.render();
      this.audio.playGunSpin();
      this.state.notify('CYLINDER_TUNE', { capacity: newCap });
    });

    // Force blank
    this.forceBlankBtn?.addEventListener('click', () => {
      const cur = this.roulette.currentRound;
      if (cur < this.roulette.totalChambers) {
        this.roulette.chambers[cur] = 0;
        // Move bullet to next or last chamber
        const nextTarget = (cur + 1) % this.roulette.totalChambers;
        this.roulette.chambers[nextTarget] = 1;
        this.render();
        this.audio.playButtonClick();
      }
    });

    // Force live round on current chamber
    this.forceLiveBtn?.addEventListener('click', () => {
      const cur = this.roulette.currentRound;
      if (cur < this.roulette.totalChambers) {
        this.roulette.forceSetBullet(cur);
        this.render();
        this.audio.playGunCock();
      }
    });

    // Diagnostic tests
    this.testBlankBtn?.addEventListener('click', () => {
      this.audio.playBlankShot();
      const container = document.getElementById('app-container');
      container?.classList.remove('anim-blank');
      void container?.offsetWidth;
      container?.classList.add('anim-blank');
    });

    this.testLiveBtn?.addEventListener('click', () => {
      this.audio.playLiveShot();
      const flash = document.getElementById('flash-overlay');
      const container = document.getElementById('app-container');

      flash?.classList.remove('flash-active');
      container?.classList.remove('anim-shake');
      void flash?.offsetWidth;

      flash?.classList.add('flash-active');
      container?.classList.add('anim-shake');
    });
  }

  toggle() {
    this.drawer?.classList.toggle('open');
    if (this.isOpen()) {
      this.render();
    }
  }

  open() {
    this.drawer?.classList.add('open');
    this.render();
  }

  close() {
    this.drawer?.classList.remove('open');
  }

  isOpen() {
    return this.drawer?.classList.contains('open');
  }

  render() {
    this.renderCylinder();
    this.renderAITelemetry();
  }

  renderCylinder() {
    if (!this.cylinderContainer) return;
    const state = this.roulette.getState();

    this.cylinderContainer.innerHTML = '';
    state.chambers.forEach((hasBullet, idx) => {
      const node = document.createElement('div');
      node.className = 'chamber-node';
      node.textContent = `#${idx + 1}`;

      if (idx === state.currentRound) {
        node.classList.add('active-chamber');
      }
      if (hasBullet === 1) {
        node.classList.add('has-bullet');
      }
      if (idx < state.currentRound) {
        node.classList.add('fired');
      }

      this.cylinderContainer.appendChild(node);
    });

    if (this.liveOddsText) {
      this.liveOddsText.textContent = state.formattedOdds;
    }

    const bulletIndex = state.chambers.indexOf(1);
    if (this.bulletChamberText) {
      this.bulletChamberText.textContent = bulletIndex !== -1 ? `Chamber #${bulletIndex + 1}` : 'None';
    }

    if (this.cylinderSummaryText) {
      this.cylinderSummaryText.textContent = `${state.totalChambers} Chambers • ${state.bulletCount} Live`;
    }
  }

  renderAITelemetry() {
    const tel = this.ai.getTelemetry();

    if (this.rockFill) this.rockFill.style.width = `${tel.distribution.rock.percentage}%`;
    if (this.paperFill) this.paperFill.style.width = `${tel.distribution.paper.percentage}%`;
    if (this.scissorsFill) this.scissorsFill.style.width = `${tel.distribution.scissors.percentage}%`;

    if (this.rockPct) this.rockPct.textContent = `${tel.distribution.rock.percentage}%`;
    if (this.paperPct) this.paperPct.textContent = `${tel.distribution.paper.percentage}%`;
    if (this.scissorsPct) this.scissorsPct.textContent = `${tel.distribution.scissors.percentage}%`;

    if (this.aiIqText) this.aiIqText.textContent = `${tel.intelligenceRate} Intel`;

    // Predict next pick
    const lastPicks = tel.lastFivePlayerPicks;
    const candidateA = lastPicks.length > 0 ? lastPicks[lastPicks.length - 1] : 1;
    const candidateB = (candidateA % 3) + 1;
    const predicted = this.ai.predictPlayerChoice(candidateA, candidateB);
    const names = { 1: 'Rock (🪨)', 2: 'Paper (📄)', 3: 'Scissors (✂️)' };

    if (this.predictedPickText) {
      this.predictedPickText.textContent = names[predicted] || 'Calculating...';
    }
  }
}

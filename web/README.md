# Two-Hand Rock-Paper-Scissors: Russian Roulette Edition

> An interactive web adaptation and evolution of the Android tactical game, built with pure HTML5, CSS3, and ES6 JavaScript. Zero build steps, zero third-party dependencies, instant loading.

---

## 🎮 Game Overview

A high-tension combination of **Two-Hand Rock-Paper-Scissors** and **Russian Roulette** survival mechanics:
1. **Phase 1: Dual Pick**: Choose 2 distinct cards out of Rock, Paper, and Scissors. The computer picks 2 cards as well.
2. **Phase 2: Reveal**: Both players expose their 2 selected hands on the table.
3. **Phase 3: The Elimination**: Each side discards one card and locks in their final card for the clash.
4. **Phase 4: Clash & Consequence**:
   - **Normal Mode**: Standard point-based tactical matches with win streak tracking.
   - **Russian Roulette Mode**: The loser of the clash must face a 6-chamber revolver with 1 live round. Survival odds change dynamically as chambers are cleared ($16.7\% \to 20\% \to 25\% \to 33.3\% \to 50\% \to 100\%$).

---

## 🚀 Live Portfolio Integration

This web demo is designed to be hosted independently on **GitHub Pages** and linked or embedded directly into your portfolio website (`portfolio/index.html`).

### Step 1: Enable GitHub Pages in this Repository
1. Push this repository to GitHub.
2. Go to **Settings** → **Pages**.
3. Under **Build and deployment** → **Source**, select **Deploy from a branch**.
4. Set branch to `main` and folder to `/ (root)` or `/docs` (if copied to docs).
5. Your live demo will be available at: `https://<your-username>.github.io/rock-paper-scissors/web/`

### Step 2: Paste into your Portfolio (`portfolio/index.html`)

#### Option A: Interactive Project Showcase Card (Recommended)
```html
<div class="project-card">
  <div class="project-badge">Game Dev / Pure JS</div>
  <h3>Two-Hand RPS: Russian Roulette</h3>
  <p>
    Tactical dual-hand card battler featuring a Russian Roulette odds engine,
    Markov-chain AI pattern recognition, and an Operator Dev Mode for recruiter observability.
  </p>
  <div class="tech-tags">
    <span>Vanilla JS (ES6)</span>
    <span>Web Audio API</span>
    <span>CSS Keyframe FX</span>
    <span>State Machine</span>
  </div>
  <div class="card-links">
    <a href="https://<your-username>.github.io/rock-paper-scissors/web/" target="_blank" class="btn btn-primary">Play Live Demo ⚔️</a>
    <a href="https://github.com/<your-username>/rock-paper-scissors" target="_blank" class="btn btn-outline">GitHub Source</a>
  </div>
</div>
```

#### Option B: Embedded In-Portfolio Iframe Modal
```html
<!-- Responsive Iframe Embed -->
<div class="game-modal-container" style="max-width: 900px; margin: auto;">
  <iframe 
    src="https://<your-username>.github.io/rock-paper-scissors/web/" 
    width="100%" 
    height="680" 
    style="border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; box-shadow: 0 20px 50px rgba(0,0,0,0.8);"
    allow="autoplay"
    title="Two-Hand Rock-Paper-Scissors Live Demo">
  </iframe>
</div>
```

---

## ⚡ Technical Highlights (For Technical Interviewers)

1. **State Machine Architecture (`js/state.js`)**:
   - Clean, decoupled state transitions across 9 distinct screens without DOM spaghetti.
2. **Adaptive AI Pattern Recognition (`js/aiEngine.js`)**:
   - Tracks player card selection frequencies and sequential Markov transitions.
   - Counter-picks based on opponent behavioral tendencies with 4 difficulty levels (Casual to Nightmarish).
3. **Roulette Probability Engine (`js/rouletteEngine.js`)**:
   - Accurately tracks chamber progression, live bullet positions, and recalculates exact conditional survival probabilities ($P = \frac{B_{remaining}}{C_{remaining}}$).
4. **Recruiter / Operator HUD (`js/operatorHUD.js`)**:
   - Click the **DEV HUD** button (or press \` / ~) to inspect real-time AI decision weights, view X-Ray cylinder bullet locations, and test gun sound effects.
5. **Web Audio & Haptics Engine (`js/audioEngine.js`)**:
   - Preloaded spatial audio effects for card shuffling, cylinder spinning, gun cocking, blank clicks, and live blasts, paired with `navigator.vibrate` mobile haptics.

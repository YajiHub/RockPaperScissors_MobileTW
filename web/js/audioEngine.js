/**
 * Audio & Haptics Engine
 * Handles sound effects, preloading, volume controls, and mobile vibration feedback
 */

const SOUND_PATHS = Object.freeze({
  BUTTON_CLICK: 'assets/audio/button_click.mp3',
  CARD_SELECT: 'assets/audio/card_select.mp3',
  GUN_SPIN: 'assets/audio/gun_spin.mp3',
  GUN_COCK: 'assets/audio/gun_cock.mp3',
  GUN_EMPTY: 'assets/audio/gun_empty.mp3',
  GUN_LOAD: 'assets/audio/gun_load.mp3',
  WIN: 'assets/audio/win.mp3',
  LOSE: 'assets/audio/lose.mp3',
  TIE: 'assets/audio/tie.mp3'
});

export class AudioEngine {
  constructor() {
    this.audioPool = {};
    this.activeAudios = new Set();
    this.isMuted = false;
    this.volume = 0.8;
    this.hapticsEnabled = true;
    this.isAudioUnlocked = false;
    this.preloadSounds();
  }

  preloadSounds() {
    for (const [key, path] of Object.entries(SOUND_PATHS)) {
      const audio = new Audio();
      audio.preload = 'auto';
      audio.src = path;
      this.audioPool[key] = audio;
    }
  }

  unlockAudio() {
    if (this.isAudioUnlocked) return;
    this.isAudioUnlocked = true;
    // Play and immediately pause silent snippet to unlock iOS/browser audio restrictions
    const dummy = this.audioPool.BUTTON_CLICK;
    if (dummy) {
      dummy.volume = 0;
      dummy.play().then(() => {
        dummy.pause();
        dummy.currentTime = 0;
      }).catch(() => {});
    }
  }

  setVolume(level) {
    this.volume = Math.max(0, Math.min(1, level));
    for (const audio of this.activeAudios) {
      audio.volume = this.volume;
    }
  }

  toggleMute() {
    this.isMuted = !this.isMuted;
    if (this.isMuted) {
      this.stopAll();
    }
    return this.isMuted;
  }

  setMuted(muted) {
    this.isMuted = Boolean(muted);
    if (this.isMuted) {
      this.stopAll();
    }
  }

  stopAll() {
    for (const audio of this.activeAudios) {
      try {
        audio.pause();
        audio.currentTime = 0;
      } catch (e) {}
    }
    this.activeAudios.clear();
  }

  play(soundKey, maxDurationMs = null) {
    if (this.isMuted) return;
    this.unlockAudio();

    const original = this.audioPool[soundKey];
    if (original) {
      // Clone audio to allow overlapping instances
      const clone = original.cloneNode();
      clone.volume = this.volume;
      this.activeAudios.add(clone);

      clone.onended = () => {
        this.activeAudios.delete(clone);
      };

      if (maxDurationMs && maxDurationMs > 0) {
        setTimeout(() => {
          try {
            clone.pause();
            clone.currentTime = 0;
          } catch (e) {}
          this.activeAudios.delete(clone);
        }, maxDurationMs);
      }

      clone.play().catch(err => {
        this.activeAudios.delete(clone);
        console.debug('Audio play skipped:', err.message);
      });
    }
  }

  /**
   * Haptic vibration for mobile users
   */
  vibrate(pattern = 50) {
    if (!this.hapticsEnabled || typeof navigator === 'undefined' || !navigator.vibrate) return;
    try {
      navigator.vibrate(pattern);
    } catch (e) {
      // Ignored
    }
  }

  // Convenience triggers
  playButtonClick() {
    this.play('BUTTON_CLICK');
    this.vibrate(25);
  }

  playCardSelect() {
    this.play('CARD_SELECT');
    this.vibrate(35);
  }

  playGunSpin() {
    this.play('GUN_SPIN');
    this.vibrate([40, 60, 40, 60, 80]);
  }

  playGunCock(maxDurationMs = 500) {
    this.play('GUN_COCK', maxDurationMs);
    this.vibrate(80);
  }

  playBlankShot() {
    this.play('GUN_EMPTY');
    this.vibrate(100);
  }

  playGunLoad(maxDurationMs = 800) {
    this.play('GUN_LOAD', maxDurationMs);
    this.vibrate(60);
  }

  playLiveShot() {
    this.play('LOSE');
    this.vibrate([150, 50, 400]);
  }

  playWin() {
    this.play('WIN');
    this.vibrate([100, 50, 100]);
  }

  playLose() {
    this.play('LOSE');
    this.vibrate(300);
  }

  playTie() {
    this.play('TIE');
    this.vibrate(70);
  }
}

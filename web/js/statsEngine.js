/**
 * Stats & Persistence Engine
 * Saves game records, win rates, and streaks to LocalStorage
 */

const STATS_KEY = 'rps_russian_roulette_stats';

const DEFAULT_STATS = Object.freeze({
  normalWins: 0,
  normalLosses: 0,
  normalTies: 0,
  russianWins: 0,
  russianDeaths: 0,
  russianSurvived: 0,
  currentStreak: 0,
  bestStreak: 0,
  totalGames: 0
});

export class StatsEngine {
  constructor() {
    this.stats = this.loadStats();
  }

  loadStats() {
    try {
      const data = localStorage.getItem(STATS_KEY);
      if (data) {
        return { ...DEFAULT_STATS, ...JSON.parse(data) };
      }
    } catch (e) {
      console.warn('LocalStorage unavailable, running with memory stats:', e);
    }
    return { ...DEFAULT_STATS };
  }

  saveStats() {
    try {
      localStorage.setItem(STATS_KEY, JSON.stringify(this.stats));
    } catch (e) {
      console.warn('Failed to save to localStorage:', e);
    }
  }

  recordNormalResult(outcome) {
    this.stats.totalGames++;
    if (outcome === 'WIN') {
      this.stats.normalWins++;
      this.stats.currentStreak++;
      if (this.stats.currentStreak > this.stats.bestStreak) {
        this.stats.bestStreak = this.stats.currentStreak;
      }
    } else if (outcome === 'LOSE') {
      this.stats.normalLosses++;
      this.stats.currentStreak = 0;
    } else {
      this.stats.normalTies++;
    }
    this.saveStats();
    return this.getSummary();
  }

  recordRussianResult(didPlayerWin, didPlayerDie, roundsSurvived) {
    this.stats.totalGames++;
    if (didPlayerWin) {
      this.stats.russianWins++;
      this.stats.currentStreak++;
      if (this.stats.currentStreak > this.stats.bestStreak) {
        this.stats.bestStreak = this.stats.currentStreak;
      }
    }

    if (didPlayerDie) {
      this.stats.russianDeaths++;
      this.stats.currentStreak = 0;
    } else {
      this.stats.russianSurvived += roundsSurvived || 1;
    }

    this.saveStats();
    return this.getSummary();
  }

  getSummary() {
    const totalWins = this.stats.normalWins + this.stats.russianWins;
    const winRate = this.stats.totalGames > 0
      ? ((totalWins / this.stats.totalGames) * 100).toFixed(1) + '%'
      : '0.0%';

    const russianTotal = this.stats.russianWins + this.stats.russianDeaths;
    const survivalRate = russianTotal > 0
      ? ((this.stats.russianWins / russianTotal) * 100).toFixed(1) + '%'
      : '100.0%';

    return {
      ...this.stats,
      totalWins,
      winRate,
      survivalRate
    };
  }

  reset() {
    this.stats = { ...DEFAULT_STATS };
    this.saveStats();
    return this.getSummary();
  }
}

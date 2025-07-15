/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

/**
 * Heads-Up Display aka HUD.
 */
public interface HUD {

    boolean isVisible();
    void show(boolean b);

    LevelCounter theLevelCounter();
    LivesCounter theLivesCounter();

    boolean isLevelCounterVisible();
    void showLevelCounter(boolean b);

    boolean isLivesCounterVisible();
    void showLivesCounter(boolean b);

    boolean isScoreVisible();
    void showScore(boolean b);

    boolean isCreditVisible();
    void showCredit(boolean b);

    // Fluent API
    default HUD all(boolean state) { return credit(state).score(state).levelCounter(state).livesCounter(state); }
    default HUD credit(boolean on) { showCredit(on); return this; }
    default HUD levelCounter(boolean on) { showLevelCounter(on); return this; }
    default HUD livesCounter(boolean on) { showLivesCounter(on); return this; }
    default HUD score(boolean on) { showScore(on); return this; }
}

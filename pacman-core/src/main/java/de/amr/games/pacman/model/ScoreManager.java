/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

public interface ScoreManager {
    Score score();
    Score highScore();
    void scorePoints(int points);
    void resetScore();
    void loadHighScore();
    void saveHighScore();
    void updateHighScore();
    void setScoreLevelNumber(int levelNumber);
    boolean isScoreVisible();
    void setScoreVisible(boolean visible);
}

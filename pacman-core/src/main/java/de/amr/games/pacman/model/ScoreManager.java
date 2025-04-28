/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import java.io.File;

public interface ScoreManager {
    Score score();
    Score highScore();
    void scorePoints(int points);
    void resetScore();
    void loadHighScore();
    void setHighScoreFile(File file);
    void resetHighScore();
    void updateHighScore();
    void setHighScoreEnabled(boolean enabled);
    void setScoreLevelNumber(int levelNumber);
    void setScoreEnabled(boolean enabled);
    boolean isScoreEnabled();
}

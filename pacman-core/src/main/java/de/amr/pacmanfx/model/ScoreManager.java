/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.util.Set;

public interface ScoreManager {
    Score score();
    Score highScore();
    void scorePoints(int points);
    void resetScore();
    void loadHighScore();
    void saveHighScore();
    void updateHighScore();
    void setGameLevelNumber(int levelNumber);
    void setExtraLifeScores(Set<Integer> scores);
}

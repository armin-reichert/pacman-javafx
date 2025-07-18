/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.io.File;
import java.util.Set;

public interface ScoreManager {
    Score score();
    Score highScore();
    void scorePoints(int points);
    void resetScore();
    void loadHighScore(File file);
    void saveHighScore(File file);
    void updateHighScore(File file);
    void setScoreLevelNumber(int levelNumber);
    void onScoreChanged(GameModel game, int oldScore, int newScore);
    void setExtraLifeScores(Set<Integer> scores);
}

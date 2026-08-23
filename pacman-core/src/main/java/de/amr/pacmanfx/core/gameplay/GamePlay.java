/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;

public interface GamePlay {

    void startSession(GameContext game);

    void prepareLevelForPlaying(GameContext game);

    void configureLevelCounter(GameContext game, LevelCounter levelCounter);

    GameLevel createLevel(GameContext game, int levelNumber);

    GameLevel buildDemoLevel(GameContext game);

    GameLevel buildNormalLevel(GameContext game, int levelNumber, int numLives);

    void startLevel(GameContext game);

    void startNextLevel(GameContext game);

    void showLevelMessage(GameContext game, GameLevel level, GameLevelMessageType type);

    // Playing level

    void updateGamePlay(GameContext game, GameLevel level);

    void onEatBonus(GameContext game, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext game, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext game, GameLevel level);

    void onLevelCompleted(GameContext game, GameLevel level);

    // Scoring

    void scorePoints(GameContext game, int points, int levelNumber);
}

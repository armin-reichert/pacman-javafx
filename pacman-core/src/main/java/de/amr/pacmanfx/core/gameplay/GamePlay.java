/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;

public interface GamePlay {

    void startSession(GameContext game);

    void prepareLevelForPlaying(GameContext game);

    void configureLevelCounter(GameContext game, LevelCounter levelCounter);

    GameLevel createLevel(GameContext game, int levelNumber);

    GameLevel buildDemoLevel(GameContext game);

    void buildNormalLevel(GameContext game, int levelNumber, int numLives);

    boolean isPacSafeInDemoLevel(GameSession session, GameLevel demoLevel);

    void startLevel(GameContext game);

    void startNextLevel(GameContext game);

    void showLevelMessage(GameContext game, GameLevel level, GameLevelMessageType type);

    // Playing level

    void hunt(GameContext game, GameLevel level);

    void updateEntities(GameContext game, GameLevel level);

    void onPacPowerStarts(GameContext game, GameLevel level, Pac pac, long ticks);

    void onPacPowerEnds(GameContext game, GameLevel level, Pac pac);

    void onPacPowerStartsFading(GameContext game, GameLevel level, Pac pac);

    void onEatPellet(GameContext game, GameLevel level, Vector2i tile);

    void onEatEnergizer(GameContext game, GameLevel level, Vector2i tile);

    void onEatBonus(GameContext game, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext game, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext game, GameLevel level);

    void onLevelCompleted(GameContext game, GameLevel level);

    // Scoring

    void scorePoints(GameContext game, int points, int levelNumber);
}

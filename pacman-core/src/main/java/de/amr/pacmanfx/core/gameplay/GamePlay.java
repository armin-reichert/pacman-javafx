/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;

public interface GamePlay {

    void startSession(GameContext game);

    void prepareLevelForPlaying(GameContext game, GameLevel level);

    void configureHUD(GameContext game, GameLevel level, HUD hud);

    GameLevel createLevel(GameContext game, int levelNumber);

    GameLevel buildDemoLevel(GameContext game);

    GameLevel buildNormalLevel(GameContext game, int levelNumber);

    void startLevel(GameContext game, GameLevel level);

    void startNextLevel(GameContext game);

    // Playing level

    boolean canStart(GameContext game);

    void update(GameContext game, GameLevel level);

    void pacEatsGhost(GameContext game, GameLevel level, Ghost eatenGhost);

    void activateNextBonus(GameContext game, GameLevel level);

    // Scoring

    void scorePoints(GameContext game, int points, int levelNumber);
}

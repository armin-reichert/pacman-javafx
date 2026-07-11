/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.core.model.level.GameLevel;

/**
 * Extension of Arcade Ms. Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    private static ArcadeMsPacMan_GameRules createRules() {
        return new ArcadeMsPacMan_GameRules() {
            @Override
            public boolean isBonusAwarded(GameLevel level) {
                final int totalFoodCount = level.worldMap().foodLayer().totalFoodCount();
                final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
                // XXL maps may have different food count, use heuristic values
                return pelletsEaten == totalFoodCount / 4 || pelletsEaten == totalFoodCount * 3 / 4;
            }
        };
    }

    public PacManXXL_MsPacMan_GameModel() {
        mapSelector = PacManXXL_MapSelector.instance();
        rules = createRules();
    }

    @Override
    public PacManXXL_MapSelector mapSelector() {
        return (PacManXXL_MapSelector) mapSelector;
    }
}
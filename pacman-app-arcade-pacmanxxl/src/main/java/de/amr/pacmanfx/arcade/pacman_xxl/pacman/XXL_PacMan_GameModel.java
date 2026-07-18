/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_MapSelector;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.FoodLayer;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static ArcadePacMan_GameRules createRules() {
        return new ArcadePacMan_GameRules() {
            @Override
            public boolean isBonusAwarded(GameLevel level) {
                final FoodLayer foodLayer = level.worldMap().foodLayer();
                final int total = foodLayer.totalFoodCount();
                final int eaten = foodLayer.eatenFoodCount();
                // XXL maps may have different food count, use heuristic values
                return eaten == total / 4 || eaten == total * 3 / 4;
            }
        };
    }

    public XXL_PacMan_GameModel() {
        mapSelector = XXL_MapSelector.instance();
        rules = createRules();
    }

    @Override
    public XXL_MapSelector mapSelector() {
        return (XXL_MapSelector) mapSelector;
    }
}
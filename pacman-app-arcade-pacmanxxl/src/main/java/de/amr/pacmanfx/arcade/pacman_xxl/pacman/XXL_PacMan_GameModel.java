/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_ScoringRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    public XXL_PacMan_GameModel() {
        worldMapManager = XXL_WorldMapManager.instance();
    }

    @Override
    public XXL_WorldMapManager worldMapManager() {
        return (XXL_WorldMapManager) worldMapManager;
    }
}
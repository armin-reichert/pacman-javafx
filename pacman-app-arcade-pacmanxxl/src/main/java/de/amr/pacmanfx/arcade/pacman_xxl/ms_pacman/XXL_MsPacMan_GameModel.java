/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;

/**
 * Extension of Arcade Ms. Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public XXL_MsPacMan_GameModel() {
        worldMapManager = XXL_WorldMapManager.instance();
    }

    @Override
    public XXL_WorldMapManager worldMapManager() {
        return (XXL_WorldMapManager) worldMapManager;
    }
}
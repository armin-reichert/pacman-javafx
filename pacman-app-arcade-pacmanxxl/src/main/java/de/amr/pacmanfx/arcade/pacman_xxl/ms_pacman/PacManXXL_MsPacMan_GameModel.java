/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public PacManXXL_MsPacMan_GameModel() {
        rules = new PacManXXL_MsPacMan_GameRules();
    }

    @Override
    public PacManXXL_MapSelector mapSelector() { return (PacManXXL_MapSelector) mapSelector; }

}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_GameRules;

public class XXL_MsPacMan_GameRules extends ArcadeMsPacMan_GameRules {

    public XXL_MsPacMan_GameRules() {
        scoringRules = new XXL_MsPacMan_ScoringRules();
    }
}

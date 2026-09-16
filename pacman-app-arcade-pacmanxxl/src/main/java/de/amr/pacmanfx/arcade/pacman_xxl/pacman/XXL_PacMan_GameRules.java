/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;

public class XXL_PacMan_GameRules extends ArcadePacMan_GameRules {

    public XXL_PacMan_GameRules() {
        scoringRules = new XXL_PacMan_ScoringRules();
    }
}

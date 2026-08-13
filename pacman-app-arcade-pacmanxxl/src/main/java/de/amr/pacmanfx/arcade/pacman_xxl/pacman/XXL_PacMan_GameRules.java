/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;


import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_ScoringRules;

public class XXL_PacMan_GameRules extends ArcadePacMan_GameRules {

    private final XXL_ScoringRules scoringRules = new XXL_ScoringRules();

    @Override
    public XXL_ScoringRules scoringRules() {
        return scoringRules;
    }
}

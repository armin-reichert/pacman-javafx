/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;


import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_ScoringRules;

public class XXL_MsPacMan_GameRules extends ArcadeMsPacMan_GameRules {

    private final XXL_ScoringRules scoringRules = new XXL_ScoringRules();

    @Override
    public ArcadePacMan_ScoringRules scoringRules() {
        return scoringRules;
    }
}

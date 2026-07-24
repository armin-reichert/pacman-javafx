/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_ScoringRules;

/**
 * Extension of Arcade Ms. Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    private static final XXL_ScoringRules XXL_SCORING_RULES = new XXL_ScoringRules();

    private static ArcadeMsPacMan_GameRules createRules() {
        return new ArcadeMsPacMan_GameRules() {
            @Override
            public ArcadePacMan_ScoringRules scoringRules() {
                return XXL_SCORING_RULES;
            }
        };
    }

    public XXL_MsPacMan_GameModel() {
        mapSelector = XXL_MapSelector.instance();
        rules = createRules();
    }

    @Override
    public XXL_MapSelector mapSelector() {
        return (XXL_MapSelector) mapSelector;
    }
}
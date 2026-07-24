/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_ScoringRules;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final XXL_ScoringRules XXL_SCORING_RULES = new XXL_ScoringRules();

    private static ArcadePacMan_GameRules createRules() {
        return new ArcadePacMan_GameRules() {
            @Override
            public ArcadePacMan_ScoringRules scoringRules() {
                return XXL_SCORING_RULES;
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
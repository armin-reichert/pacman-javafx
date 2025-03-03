/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    private final PacManXXL_MapSelector xxlMapSelector;

    public PacManXXL_MsPacMan_GameModel(PacManXXL_MapSelector mapSelector) {
        xxlMapSelector = mapSelector;
    }

    @Override
    public void init() {
        super.init();
        demoLevelSteering = new RuleBasedPacSteering(this); // super class uses predefined steering
        scoreManager.setHighScoreFile(new File(HOME_DIR, "highscore-mspacman_xxl.xml"));
        mapSelector = xxlMapSelector;
    }

    public PacManXXL_MapSelector mapSelector() {
        return xxlMapSelector;
    }
}
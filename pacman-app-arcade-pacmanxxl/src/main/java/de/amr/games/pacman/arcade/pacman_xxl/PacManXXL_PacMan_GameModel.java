/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.arcade.ArcadePacMan_GameModel;
import de.amr.games.pacman.model.MapSelector;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    public PacManXXL_PacMan_GameModel(MapSelector mapSelector) {
        super(mapSelector);
    }

    public void init() {
        super.init();
        demoLevelSteering = new RuleBasedPacSteering(this); // super class uses predefined steering
        scoreManager.setHighScoreFile(new File(Globals.HOME_DIR, "highscore-pacman_xxl.xml"));
        mapSelector.loadAllMaps(this);
    }

    @Override
    public void resetEverything() {
        super.resetEverything();
    }
}
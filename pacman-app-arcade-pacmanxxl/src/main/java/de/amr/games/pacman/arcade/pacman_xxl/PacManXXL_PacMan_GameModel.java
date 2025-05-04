/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.arcade.ArcadePacMan_GameModel;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.model.MapSelector;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;

import static de.amr.games.pacman.Globals.THE_RNG;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    public PacManXXL_PacMan_GameModel(MapSelector mapSelector) {
        super(mapSelector);
        highScoreFile = new File(Globals.HOME_DIR, "highscore-pacman_xxl.xml");
        // Demo level map could be custom map, so use generic automatic steering
        demoLevelSteering = new RuleBasedPacSteering(this);
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[THE_RNG.nextInt(levelNumbers.length)];
        MapSelectionMode mapSelectionMode = mapSelector.mapSelectionMode();
        mapSelector.setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createNewLevel(levelNumber);
        level.setData(createLevelData(1)); // overwrite to always run as fast as first level
        mapSelector.setMapSelectionMode(mapSelectionMode);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
    }
}
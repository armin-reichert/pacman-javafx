/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.model.MapSelector;

import java.io.File;

import static de.amr.games.pacman.Globals.THE_RNG;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public PacManXXL_MsPacMan_GameModel(MapSelector mapSelector) {
        super(mapSelector);
        highScoreFile = new File(Globals.HOME_DIR, "highscore-mspacman_xxl.xml");
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[THE_RNG.nextInt(levelNumbers.length)];
        MapSelectionMode mapSelectionMode = mapSelector.mapSelectionMode();
        mapSelector.setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        newLevel(levelNumber, createLevelData(1));
        mapSelector.setMapSelectionMode(mapSelectionMode);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
    }
}
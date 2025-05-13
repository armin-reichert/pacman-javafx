/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;

import java.io.File;

import static de.amr.pacmanfx.Globals.theRNG;

public class XXLMsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public XXLMsPacMan_GameModel(MapSelector mapSelector) {
        super(mapSelector);
        scoreManager.setHighScoreFile(new File(Globals.HOME_DIR, "highscore-mspacman_xxl.xml"));
    }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[theRNG().nextInt(levelNumbers.length)];
        MapSelectionMode mapSelectionMode = mapSelector.mapSelectionMode();
        mapSelector.setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        level.setData(createLevelData(1)); // overwrite to always run as fast as first level
        mapSelector.setMapSelectionMode(mapSelectionMode);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
    }
}
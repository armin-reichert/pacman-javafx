/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.MapSelectionMode;

import java.io.File;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static de.amr.pacmanfx.Globals.theRNG;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public PacManXXL_MsPacMan_GameModel(PacManXXL_Common_MapSelector mapSelector) {
        super(mapSelector);
        scoreManager.setHighScoreFile(new File(Globals.HOME_DIR, "highscore-mspacman_xxl.xml"));
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[theRNG().nextInt(levelNumbers.length)];
        mapSelector().setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        level.setData(createLevelData(1)); // always run with settings (speed etc.) of first level
        level.setDemoLevel(true);
        level.huntingTimer().reset();
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotAlgorithm(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
        scoreManager.setScoreLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
    }
}
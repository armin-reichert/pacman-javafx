/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;

import java.io.File;
import java.util.Random;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    public PacManXXL_MsPacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File higScoreFile) {
        super(gameContext, mapSelector, higScoreFile);
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[new Random().nextInt(levelNumbers.length)];
        mapSelector().setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        level.setData(createLevelData(1)); // use settings (speed etc.) of first level
        level.setDemoLevel(true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        theHUD().theLevelCounter().setEnabled(false);
        huntingTimer.reset();
        scoreManager().setScoreLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }
}
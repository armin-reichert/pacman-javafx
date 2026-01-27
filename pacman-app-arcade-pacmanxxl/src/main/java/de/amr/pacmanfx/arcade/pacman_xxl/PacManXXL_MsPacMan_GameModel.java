/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.model.world.WorldMapSelector;

import java.io.File;
import java.util.Random;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_MsPacMan_GameModel(CoinMechanism coinMechanism, WorldMapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, mapSelector, highScoreFile);
    }

    @Override
    public PacManXXL_MapSelector mapSelector() { return (PacManXXL_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[new Random().nextInt(levelNumbers.length)];
        mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);
        final GameLevel level = createLevel(levelNumber, true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        setLevelCounterEnabled(false);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(new LevelCreatedEvent(level));
    }
}
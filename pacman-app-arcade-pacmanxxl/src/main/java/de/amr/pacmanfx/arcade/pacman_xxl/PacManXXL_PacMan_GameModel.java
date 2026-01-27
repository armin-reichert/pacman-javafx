/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import java.io.File;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    public PacManXXL_PacMan_GameModel(CoinMechanism coinMechanism, WorldMapSelector mapSelector, File highScoreFile) {
        super(coinMechanism, mapSelector, highScoreFile);
        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        demoLevelSteering = new RuleBasedPacSteering();
    }

    @Override
    public PacManXXL_MapSelector mapSelector() { return (PacManXXL_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int randomIndex = randomInt(0, DEMO_LEVEL_NUMBERS.length);
        int levelNumber = DEMO_LEVEL_NUMBERS[randomIndex];
        score().setLevelNumber(levelNumber);
        mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        final GameLevel level = createLevel(levelNumber, true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();
        setLevelCounterEnabled(false);
        gateKeeper.setLevelNumber(levelNumber);

        levelProperty().set(level);
        publishGameEvent(new LevelCreatedEvent(level));
    }
}
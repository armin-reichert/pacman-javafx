/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import java.io.File;

import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_PacMan_GameModel(GameContext gameContext, MapSelector mapSelector, File highScoreFile) {
        super(gameContext, mapSelector, highScoreFile);
        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        demoLevelSteering = new RuleBasedPacSteering(gameContext);
    }

    @Override
    public PacManXXL_Common_MapSelector mapSelector() { return (PacManXXL_Common_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int randomIndex = randomInt(0, DEMO_LEVEL_NUMBERS.length);
        int levelNumber = DEMO_LEVEL_NUMBERS[randomIndex];
        scoreManager().setGameLevelNumber(levelNumber);
        mapSelector().setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        gameLevel.setData(createLevelData(1)); // always run with settings (speed etc.) of first level
        gameLevel.setDemoLevel(true);
        gameLevel.pac().setImmune(false);
        gameLevel.pac().setUsingAutopilot(true);
        gameLevel.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        setLevelCounterEnabled(false);
        huntingTimer().reset();
        gateKeeper.setLevelNumber(levelNumber);
        gameLevel.house().ifPresent(house -> gateKeeper.setHouse(house));
        eventManager().publishEvent(GameEventType.LEVEL_CREATED);
    }
}
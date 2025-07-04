/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import java.io.File;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static de.amr.pacmanfx.Globals.theRNG;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    public PacManXXL_PacMan_GameModel(PacManXXL_Common_MapSelector mapSelector) {
        super(mapSelector);
        setHighScoreFile(new File(Globals.HOME_DIR, "highscore-pacman_xxl.xml"));
        // Demo level map could be custom map, so use generic automatic steering
        demoLevelSteering = new RuleBasedPacSteering(this);
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
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        hud().levelCounter().setEnabled(false);
        huntingTimer.reset();
        setScoreLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        level.house().ifPresent(house -> gateKeeper.setHouse(house)); //TODO what if no house exists?
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
    }
}
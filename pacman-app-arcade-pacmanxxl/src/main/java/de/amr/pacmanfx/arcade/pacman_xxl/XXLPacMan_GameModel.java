/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import java.io.File;

import static de.amr.pacmanfx.Globals.theRNG;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class XXLPacMan_GameModel extends ArcadePacMan_GameModel {

    public XXLPacMan_GameModel(XXLAnyPacMan_MapSelector mapSelector) {
        super(mapSelector);
        scoreManager.setHighScoreFile(new File(Globals.HOME_DIR, "highscore-pacman_xxl.xml"));
        // Demo level map could be custom map, so use generic automatic steering
        demoLevelSteering = new RuleBasedPacSteering(this);
    }

    @Override
    public XXLAnyPacMan_MapSelector mapSelector() { return (XXLAnyPacMan_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        // Select random (standard) level with different map and map color scheme for each choice
        int[] levelNumbers = { 1, 3, 6, 10, 14, 18 };
        int levelNumber = levelNumbers[theRNG().nextInt(levelNumbers.length)];
        mapSelector().setMapSelectionMode(MapSelectionMode.NO_CUSTOM_MAPS);
        createLevel(levelNumber);
        level.setData(createLevelData(1)); // overwrite to always run as fast as first level
        level.setDemoLevel(true);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotAlgorithm(demoLevelSteering);
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
    }
}
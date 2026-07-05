/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;


import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

public class PacManXXL_PacMan_GamePlay extends ArcadePacMan_GamePlay {

    private static final int[] DEMOLEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    @Override
    public GameLevel buildDemoLevel(GameEventManager eventManager, GameModel model) {
        final PacManXXL_PacMan_GameModel xxlModel = (PacManXXL_PacMan_GameModel) model;

        xxlModel.mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMOLEVEL_NUMBERS[randomInt(0, DEMOLEVEL_NUMBERS.length)];
        final GameLevel level = model.createLevel(levelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        final var demoLevelSteering = new RuleBasedPacSteering();
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(levelNumber);
        model.levelCounter().setEnabled(false);
        model.score().setLevelNumber(levelNumber);

        return level;
    }
}

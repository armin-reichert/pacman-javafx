/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GamePlay;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.core.simulation.GamePlayContext;
import de.amr.pacmanfx.core.steering.RuleBasedPacSteering;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

public class PacManXXL_MsPacMan_GamePlay extends ArcadeMsPacMan_GamePlay {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    @Override
    public GameLevel buildDemoLevel(GamePlayContext playContext) {
        final PacManXXL_MsPacMan_GameModel xxlModel = (PacManXXL_MsPacMan_GameModel) playContext.model();

        xxlModel.mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMO_LEVEL_NUMBERS[randomInt(0, DEMO_LEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(xxlModel, levelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        final var demoLevelSteering = new RuleBasedPacSteering();
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        xxlModel.gateKeeper().setLevelNumber(levelNumber);
        xxlModel.levelCounter().setEnabled(false);
        xxlModel.score().setLevelNumber(levelNumber);

        return level;
    }
}

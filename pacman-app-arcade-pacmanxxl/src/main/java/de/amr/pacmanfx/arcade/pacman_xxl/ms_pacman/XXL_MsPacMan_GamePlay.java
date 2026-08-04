/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GamePlay;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.LevelCounterSystem;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class XXL_MsPacMan_GamePlay extends ArcadeMsPacMan_GamePlay {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    @Override
    public GameLevel buildDemoLevel(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameSystems sys = gameContext.systems();
        final XXL_MsPacMan_GameModel xxlModel = (XXL_MsPacMan_GameModel) gameContext.model();

        xxlModel.mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMO_LEVEL_NUMBERS[randomInt(0, DEMO_LEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(gameContext, levelNumber, true);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        final var steering = new RuleGuidedPacSteering(
            sys.worldNavigator(),
            sys.pacWorldMovementPolicy(),
            sys.pacPower()
        );
        pac.autoSteering().setSteering(steering);

        xxlModel.gateKeeper().setLevelNumber(levelNumber);
        xxlModel.score().setLevelNumber(levelNumber);

        LevelCounterSystem.enable(xxlModel.levelCounter(), false);

        return level;
    }
}

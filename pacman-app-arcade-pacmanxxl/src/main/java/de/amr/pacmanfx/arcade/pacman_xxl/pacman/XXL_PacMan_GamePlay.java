/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;


import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class XXL_PacMan_GamePlay extends ArcadePacMan_GamePlay {

    private static final int[] DEMOLEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    @Override
    public GameLevel buildDemoLevel(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSession session = gameContext.session();
        final GameSystems sys = gameContext.systems();
        final XXL_PacMan_GameModel xxlModel = (XXL_PacMan_GameModel) gameContext.model();

        xxlModel.worldMapManager().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMOLEVEL_NUMBERS[randomInt(0, DEMOLEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(gameContext, levelNumber);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        final var steering = new RuleGuidedPacSteering(
            sys.worldNavigator(),
            sys.pacWorldMovementPolicy()
        );
        pac.autoSteering().setSteering(steering);

        session.setLevel(level);
        session.setAttractMode(true);

        session.gateKeeper().setLevelNumber(levelNumber);
        ScoreSystem.setLevelNumber(session.score(), levelNumber);
        LevelCounterSystem.enable(session.levelCounter(), false);

        return level;
    }
}

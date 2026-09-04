/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;

import static de.amr.basics.math.RandomNumbers.randomInt;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.Objects.requireNonNull;

public class XXL_MsPacMan_GamePlay extends ArcadeMsPacMan_GamePlay {

    private static final int[] DEMO_LEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        final GameLevel level = super.createLevel(game, levelNumber);

        // Adapt HUD element positions to map size
        final WorldMap worldMap = level.worldMap();
        final HUD hud = game.session().hud();

        hud.levelCounter().pos().set(
            (worldMap.numCols() - 4) * TS,
            (worldMap.numRows() - 2) * TS + 2);

        hud.livesCounter().pos().set(
            2 * TS,
            (worldMap.numRows() - 2) * TS
        );

        return level;
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variant().systems();

        final XXL_WorldMapManager mapManager = (XXL_WorldMapManager) game.variant().worldMapManager();
        mapManager.setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMO_LEVEL_NUMBERS[randomInt(0, DEMO_LEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(game, levelNumber);

        final Pac pac = level.entities().pac();
        pac.autoSteering().setSteering(new RuleGuidedPacSteering(systems.navigator(), systems.pacWorldMovementPolicy()));
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        session.setLevel(level);
        session.setAttractMode(true);

        session.hud().gameScore().data().setLevelNumber( levelNumber);
        session.hud().levelCounter().data().setEnabled(false);

        return level;
    }
}

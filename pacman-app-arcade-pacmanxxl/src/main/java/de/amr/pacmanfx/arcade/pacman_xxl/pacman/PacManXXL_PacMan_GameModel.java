/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

/**
 * Extension of Arcade Pac-Man with 8 new builtin mazes (thanks to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>) and the possibility to
 * play custom maps.
 */
public class PacManXXL_PacMan_GameModel extends ArcadePacMan_GameModel {

    private static final int[] DEMOLEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    public PacManXXL_PacMan_GameModel(WorldMapSelector mapSelector) {
        super(mapSelector);
        // Demo level map could be a custom map, so use generic auto-steering that also can cope with dead-ends:
        demoLevelSteering = new RuleBasedPacSteering();
    }

    @Override
    public PacManXXL_MapSelector mapSelector() { return (PacManXXL_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel(GameContext gameContext) {
        mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMOLEVEL_NUMBERS[randomInt(0, DEMOLEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(gameContext, levelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(levelNumber);
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
        score.setLevelNumber(levelNumber);

        setLevel(level);
        gameContext.gameFlow().publishGameEvent(new LevelCreatedEvent(gameContext, level));
    }
}
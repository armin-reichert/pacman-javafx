/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.model.world.WorldMapSelector;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

public class PacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    private static final int[] DEMOLEVEL_NUMBERS = { 1, 3, 6, 10, 14, 18 };

    // Warning: Constructor signature is used via reflection by GameUI_Builder, do not change!
    public PacManXXL_MsPacMan_GameModel(GameFlow flow, CoinMechanism coinMechanism, WorldMapSelector mapSelector) {
        super(flow, coinMechanism, mapSelector);
        rules = new PacManXXL_MsPacMan_GameRules();
    }

    @Override
    public PacManXXL_MapSelector mapSelector() { return (PacManXXL_MapSelector) mapSelector; }

    @Override
    public void buildDemoLevel() {
        mapSelector().setSelectionMode(WorldMapSelectionMode.NO_CUSTOM_MAPS);

        // Select random (standard) level with different map and map color scheme for each choice
        final int levelNumber = DEMOLEVEL_NUMBERS[randomInt(0, DEMOLEVEL_NUMBERS.length)];
        final GameLevel level = createLevel(levelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(levelNumber);
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
        score.setLevelNumber(levelNumber);

        setLevel(level);
        flow.publishGameEvent(new LevelCreatedEvent(flow.context(), level));
    }
}
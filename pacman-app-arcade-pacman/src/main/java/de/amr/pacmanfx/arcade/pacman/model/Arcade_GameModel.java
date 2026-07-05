/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

import static de.amr.pacmanfx.model.world.WorldMap.tile;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 *
 * @see <a href="https://pacman.holenet.info/">The Pac-Man Dossier by Jamey Pittman</a>
 */
public abstract class Arcade_GameModel extends AbstractGameModel {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    protected Arcade_GameModel() {
        actorSpeedControl = new Arcade_ActorSpeedControl();
    }

    // Game interface

    @Override
    public void buildNormalLevel(GameContext context, int levelNumber) {
        final GameLevel level = createLevel(levelNumber, false);
        levelCounter().setEnabled(true);
        score().setLevelNumber(levelNumber);
        gateKeeper.setLevelNumber(levelNumber);
        setLevel(level);
        context.flow().publishGameEvent(new LevelCreatedEvent(context, level));
    }

    @Override
    public GameLevel buildDemoLevel() {
        final int demoLevelNumber = 1;
        final GameLevel level = createLevel(demoLevelNumber, true);
        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(demoLevelNumber);
        demoLevelSteering.init();
        levelCounter.setEnabled(true);
        score.setLevelNumber(demoLevelNumber);

        return level;
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.model.actors.Elroy;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMapSelector;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man Arcade game.
 *
 * <p>There are slight differences to the original Arcade game.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 */
public class ArcadeMsPacMan_GameModel extends GameModel {

    protected ArcadeMsPacMan_GameRules rules;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_MapSelector());
    }

    public ArcadeMsPacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        rules = new ArcadeMsPacMan_GameRules();
        levelCounter = new ArcadeMsPacMan_LevelCounter();
        configureGateKeeper();
    }

    @Override
    public void init() {
        mapSelector().loadMapPrototypes();
        setInitialLifeCount(3);
    }

    @Override
    public ArcadeMsPacMan_LevelCounter levelCounter() {
        return (ArcadeMsPacMan_LevelCounter) levelCounter;
    }

    @Override
    public GameRules rules() {
        return rules;
    }

    // Helpers

    private void configureGateKeeper() {
        gateKeeper.setOnGhostReleased((level, releasedPrisoner) -> {
            if (releasedPrisoner.personality() == ORANGE_GHOST_POKEY) {
                final Ghost blinky = level.ghost(RED_GHOST_SHADOW);
                if (blinky.elroy().boost() != Elroy.Boost.NONE && !blinky.elroy().enabled()) {
                    blinky.elroy().setEnabled(true);
                    Logger.debug("Re-enabled {}'s Elroy state ({}). Reason; ({} got released):",
                        blinky.name(), blinky.elroy(), releasedPrisoner.name());
                }
            }
        });
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.AbstractGameModel;
import de.amr.pacmanfx.core.model.GameRules;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.actors.Elroy;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.*;
import de.amr.pacmanfx.core.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.WorldMap.tile;
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
public class ArcadeMsPacMan_GameModel extends AbstractGameModel {

    protected static final int GAME_OVER_STATE_TICKS = 150;

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    protected final HUDState hudState;

    protected final GateKeeper gateKeeper;

    protected final ArcadeMsPacMan_LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected ArcadeMsPacMan_GameRules rules;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_MapSelector());
    }

    public ArcadeMsPacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        hudState = new HUDState();
        gateKeeper = new GateKeeper();
        rules = new ArcadeMsPacMan_GameRules();
        levelCounter = new ArcadeMsPacMan_LevelCounter();
        automaticSteering = new RuleBasedPacSteering();
        configureGateKeeper();
    }

    @Override
    public void init() {
        mapSelector().loadMapPrototypes();
        lives().setInitialCount(3);
        hudState().hideIt();
    }

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public HUDState hudState() {
        return hudState;
    }

    @Override
    public ArcadeMsPacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public WorldMapSelector mapSelector() {
        return mapSelector;
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
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;

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
public class ArcadeMsPacMan_GameModel implements GameModel {

    protected WorldMapManager worldMapManager;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_WorldMapManager());
    }

    public ArcadeMsPacMan_GameModel(WorldMapManager worldMapManager) {
        this.worldMapManager = requireNonNull(worldMapManager);
    }

    @Override
    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tile;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GameModel implements GameModel {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    public static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    protected WorldMapManager worldMapManager;

    public ArcadePacMan_GameModel() {
        this(new ArcadePacMan_WorldMapManager());
    }

    /**
     * @param worldMapManager e.g. selector that selects custom maps before standard maps
     */
    public ArcadePacMan_GameModel(WorldMapManager worldMapManager) {
        this.worldMapManager = requireNonNull(worldMapManager);
    }

    @Override
    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }
}
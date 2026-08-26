/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;

import java.util.Set;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.halfTileRightOf;
import static java.util.Objects.requireNonNull;

public class GhostWorldInfoComp implements GameEntityComp {

    private House house;

    private Set<Vector2i> specialTerrainTiles = Set.of();

    private Vector2f startPosition;

    public GhostWorldInfoComp() {}

    @Override
    public String toString() {
        return "GhostWorldInfo{" +
            "house=" + house +
            ", specialTerrainTiles=" + specialTerrainTiles +
            ", startPosition=" + startPosition +
            '}';
    }

    public void init(TerrainLayer terrain, House house, String startTileProperty) {
        init(terrain, house, startTileProperty, Set.of());
    }

    public void init(TerrainLayer terrain, House house, String startTileProperty, Set<Vector2i> specialTerrainTiles) {
        requireNonNull(terrain);
        requireNonNull(house);
        requireNonNull(startTileProperty);
        requireNonNull(specialTerrainTiles);

        final Vector2i tile = terrain.getTileProperty(startTileProperty); // TODO what if null?

        setHouse(house);
        setStartPosition(halfTileRightOf(tile));
        setSpecialTerrainTiles(specialTerrainTiles);
    }

    public House house() {
        return house;
    }

    public void setHouse(House house) {
        requireNonNull(house);
        this.house = house;
    }

    public void setSpecialTerrainTiles(Set<Vector2i> tiles) {
        requireNonNull(tiles);
        specialTerrainTiles = Set.copyOf(tiles);
    }

    public Set<Vector2i> specialTerrainTiles() {
        return specialTerrainTiles;
    }

    public void setStartPosition(Vector2f startPosition) {
        this.startPosition = requireNonNull(startPosition);
    }

    public Vector2f startPosition() {
        return startPosition;
    }
}

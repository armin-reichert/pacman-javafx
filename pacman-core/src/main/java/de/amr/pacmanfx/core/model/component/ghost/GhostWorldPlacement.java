/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.component.ActorComponent;
import de.amr.pacmanfx.core.model.world.House;

import java.util.Set;

public class GhostWorldPlacement implements ActorComponent {

    private House house;

    private Set<Vector2i> specialTerrainTiles = Set.of();

    private Vector2f startPosition;

    public House house() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public void setSpecialTerrainTiles(Set<Vector2i> tiles) {
        specialTerrainTiles = Set.copyOf(tiles);
    }

    public Set<Vector2i> specialTerrainTiles() {
        return specialTerrainTiles;
    }

    public void setStartPosition(Vector2f startPosition) {
        this.startPosition = startPosition;
    }

    public Vector2f startPosition() {
        return startPosition;
    }

    @Override
    public void reset() {}
}

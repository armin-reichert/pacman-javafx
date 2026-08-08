/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.house.comp.HouseFloorplanComp;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.HTS;
import static java.util.Objects.requireNonNull;

public class House extends GameEntity {

    public House() {
        setComp(HouseFloorplanComp.class, new HouseFloorplanComp());
    }

    public HouseFloorplanComp floorplan() {
        return requireComp(HouseFloorplanComp.class);
    }

    public Vector2i sizeInTiles() {
        final HouseFloorplanComp fp = requireComp(HouseFloorplanComp.class);
        return fp.maxTile().minus(fp.minTile()).plus(1, 1);
    }

    public boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        final HouseFloorplanComp fp = requireComp(HouseFloorplanComp.class);
        return tile.equals(fp.leftDoorTile()) || tile.equals(fp.rightDoorTile());
    }

    /**
     * @return center position under house, used e.g. as anchor for level messages
     */
    public Vector2f centerPositionUnderHouse() {
        final HouseFloorplanComp fp = requireComp(HouseFloorplanComp.class);
        Vector2i sizeTiles = sizeInTiles();
        return vec2_float(
            WorldMap.TS * (fp.minTile().x() + 0.5f * sizeTiles.x()),
            WorldMap.TS * (fp.minTile().y() +        sizeTiles.y())
        );
    }

    public boolean contains(Vector2i tile) {
        requireNonNull(tile);
        final HouseFloorplanComp fp = requireComp(HouseFloorplanComp.class);
        return tile.x() >= fp.minTile().x() && tile.x() <= fp.maxTile().x()
            && tile.y() >= fp.minTile().y() && tile.y() <= fp.maxTile().y();
    }

    /**
     * @param actor some actor
     * @return tells if the given actor is located inside the house
     */
    public boolean isVisitedBy(GameEntity actor) {
        requireNonNull(actor);
        final Vector2i actorTile = WorldNavigationSystem.computeTile(actor);
        return contains(actorTile);
    }

    public Vector2f center() {
        final HouseFloorplanComp fp = requireComp(HouseFloorplanComp.class);
        return fp.minTile().toVector2f().scaled(WorldMap.TS).plus(sizeInTiles().toVector2f().scaled(HTS));
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.math.Vector2f.vec2_float;
import static java.util.Objects.requireNonNull;

public class House extends GameEntity {

    private final Door door;

    public House(HouseFloorplanComp floorplan) {
        requireNonNull(floorplan);
        setComponent(HouseFloorplanComp.class, floorplan);

        final Vector2f doorPos = floorplan.leftDoorTile().toVector2f().scaled(TS);
        door = new Door();
        door.assertComponent(DoorDataComp.class).setLeftTile(floorplan.leftDoorTile());
        door.assertComponent(DoorDataComp.class).setRightTile(floorplan.rightDoorTile());
        door.pos().set(doorPos);
        door.show();
    }

    public HouseFloorplanComp floorplan() {
        return assertComponent(HouseFloorplanComp.class);
    }

    public Door door() {
        return door;
    }

    public Vector2i sizeInTiles() {
        final HouseFloorplanComp fp = assertComponent(HouseFloorplanComp.class);
        return fp.maxTile().minus(fp.minTile()).plus(1, 1);
    }

    public boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        final var doorLayout = door.assertComponent(DoorDataComp.class);
        return doorLayout.leftTile().equals(tile) || doorLayout.rightTile().equals(tile);
    }

    /**
     * @return center position under house, used e.g. as anchor for level messages
     */
    public Vector2f centerPositionUnderHouse() {
        final HouseFloorplanComp fp = assertComponent(HouseFloorplanComp.class);
        Vector2i sizeTiles = sizeInTiles();
        return vec2_float(
            TS * (fp.minTile().x() + 0.5f * sizeTiles.x()),
            TS * (fp.minTile().y() +        sizeTiles.y())
        );
    }

    public boolean contains(Vector2i tile) {
        requireNonNull(tile);
        final HouseFloorplanComp fp = assertComponent(HouseFloorplanComp.class);
        return tile.x() >= fp.minTile().x() && tile.x() <= fp.maxTile().x()
            && tile.y() >= fp.minTile().y() && tile.y() <= fp.maxTile().y();
    }

    /**
     * @param actor some actor
     * @return tells if the given actor is located inside the house
     */
    public boolean isVisitedBy(GameEntity actor) {
        requireNonNull(actor);
        final Vector2i actorTile = actor.pos().tile();
        return contains(actorTile);
    }

    public Vector2f center() {
        final HouseFloorplanComp fp = assertComponent(HouseFloorplanComp.class);
        return fp.minTile().toVector2f().scaled(TS).plus(sizeInTiles().toVector2f().scaled(HTS));
    }
}

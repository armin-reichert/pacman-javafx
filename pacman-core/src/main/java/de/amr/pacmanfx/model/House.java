/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.GhostID;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public interface House {

    byte[][] content();

    Vector2i minTile();

    Vector2i maxTile();

    Vector2i leftDoorTile();

    Vector2i rightDoorTile();

    void setGhostRevivalTile(GhostID ghostID, Vector2i tile);

    Vector2i ghostRevivalTile(GhostID ghostID);

    default Vector2i sizeInTiles() {
        return maxTile().minus(minTile()).plus(1, 1);
    }

    default boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        return tile.equals(leftDoorTile()) || tile.equals(rightDoorTile());
    }


    /**
     * @return center position under house, used e.g. as anchor for level messages
     */
    default Vector2f centerPositionUnderHouse() {
        Vector2i sizeTiles = sizeInTiles();
        return Vector2f.of(
            TS * (minTile().x() + 0.5f * sizeTiles.x()),
            TS * (minTile().y() +        sizeTiles.y())
        );
    }

    default boolean isTileInHouseArea(Vector2i tile) {
        requireNonNull(tile);
        return tile.x() >= minTile().x() && tile.x() <= maxTile().x()
            && tile.y() >= minTile().y() && tile.y() <= maxTile().y();
    }

    /**
     * @param actor some actor
     * @return tells if the given actor is located inside the house
     */
    default boolean isVisitedBy(Actor actor) {
        return isTileInHouseArea(requireNonNull(actor).tile());
    }

    /**
     * @return position at which ghosts can enter the house, one tile above and horizontally between the two door tiles
     */
    default Vector2f entryPosition() {
        return Vector2f.of(
            TS * rightDoorTile().x() - HTS,
            TS * (rightDoorTile().y() - 1));
    }

    default Vector2f center() {
        return minTile().toVector2f().scaled(TS).plus(sizeInTiles().toVector2f().scaled(HTS));
    }
}
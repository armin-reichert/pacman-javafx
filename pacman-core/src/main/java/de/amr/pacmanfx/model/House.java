package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.GhostID;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public abstract class House {

    private final Vector2i minTile;
    private final Vector2i maxTile;
    private final Vector2i leftDoorTile;
    private final Vector2i rightDoorTile;
    private final Map<GhostID, Vector2i> ghostRevivalTileMap = new HashMap<>();

    protected House(Vector2i minTile, Vector2i maxTile, Vector2i leftDoorTile, Vector2i rightDoorTile) {
        this.minTile = requireNonNull(minTile);
        this.maxTile = requireNonNull(maxTile);
        this.leftDoorTile = requireNonNull(leftDoorTile);
        this.rightDoorTile = requireNonNull(rightDoorTile);
    }

    public abstract byte[][] content();

    public Vector2i minTile() {
        return minTile;
    }

    public Vector2i maxTile() {
        return maxTile;
    }

    public Vector2i leftDoorTile() {
        return leftDoorTile;
    }

    public Vector2i rightDoorTile() {
        return rightDoorTile;
    }

    public Vector2i sizeInTiles() {
        return maxTile.minus(minTile).plus(1, 1);
    }

    public boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public void setGhostRevivalTile(GhostID ghostID, Vector2i tile) {
        requireNonNull(tile);
        ghostRevivalTileMap.put(ghostID, tile);
    }

    public Vector2i ghostRevivalTile(GhostID ghostID) {
        return ghostRevivalTileMap.get(ghostID);
    }

    /**
     * @return center position under house, used e.g. as anchor for level messages
     */
    public Vector2f centerPositionUnderHouse() {
        Vector2i sizeTiles = sizeInTiles();
        return Vector2f.of(
            TS * (minTile.x() + 0.5f * sizeTiles.x()),
            TS * (minTile.y() +        sizeTiles.y())
        );
    }

    public boolean isTileInHouseArea(Vector2i tile) {
        requireNonNull(tile);
        return tile.x() >= minTile.x() && tile.x() <= maxTile.x()
            && tile.y() >= minTile.y() && tile.y() <= maxTile.y();
    }

    /**
     * @param actor some actor
     * @return tells if the given actor is located inside the house
     */
    public boolean isVisitedBy(Actor actor) {
        return isTileInHouseArea(requireNonNull(actor).tile());
    }

    /**
     * @return position at which ghosts can enter the house, one tile above and horizontally between the two door tiles
     */
    public Vector2f entryPosition() {
        return Vector2f.of(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f center() {
        return minTile.toVector2f().scaled(TS).plus(sizeInTiles().toVector2f().scaled(HTS));
    }
}
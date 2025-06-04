/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.Objects;

public record ObstacleSegment(Vector2i startPoint, Vector2i vector, boolean ccw, byte encoding) {

    public ObstacleSegment {
        Objects.requireNonNull(startPoint);
        Objects.requireNonNull(vector);
    }

    public Vector2i endPoint() {
        return startPoint.plus(vector);
    }

    public boolean isRoundedNWCorner() { return encoding == TerrainTile.ARC_NW.byteValue(); }
    public boolean isRoundedSWCorner() { return encoding == TerrainTile.ARC_SW.byteValue(); }
    public boolean isRoundedSECorner() { return encoding == TerrainTile.ARC_SE.byteValue(); }
    public boolean isRoundedNECorner() { return encoding == TerrainTile.ARC_NE.byteValue(); }

    public boolean isAngularNWCorner() { return encoding == TerrainTile.DCORNER_NW.byteValue(); }
    public boolean isAngularSWCorner() { return encoding == TerrainTile.DCORNER_SW.byteValue(); }
    public boolean isAngularSECorner() { return encoding == TerrainTile.DCORNER_SE.byteValue();}
    public boolean isAngularNECorner() { return encoding == TerrainTile.DCORNER_NE.byteValue(); }

    public boolean isNWCorner() {
        return isRoundedNWCorner() || isAngularNWCorner();
    }
    public boolean isSWCorner() {
        return isRoundedSWCorner() || isAngularSWCorner();
    }
    public boolean isSECorner() {
        return isRoundedSECorner() || isAngularSECorner();
    }
    public boolean isNECorner() {
        return isRoundedNECorner() || isAngularNECorner();
    }

    public boolean isVerticalLine() { return encoding == TerrainTile.WALL_V.byteValue(); }
    public boolean isHorizontalLine() { return encoding == TerrainTile.WALL_H.byteValue(); }
    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

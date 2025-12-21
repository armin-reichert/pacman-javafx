/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;

import java.util.Objects;

public record ObstacleSegment(Vector2i startPoint, Vector2i vector, boolean ccw, byte encoding) {

    public ObstacleSegment {
        Objects.requireNonNull(startPoint);
        Objects.requireNonNull(vector);
    }

    public Vector2i endPoint() {
        return startPoint.plus(vector);
    }

    public boolean isRoundedNWCorner() { return encoding == TerrainTile.ARC_NW.$; }
    public boolean isRoundedSWCorner() { return encoding == TerrainTile.ARC_SW.$; }
    public boolean isRoundedSECorner() { return encoding == TerrainTile.ARC_SE.$; }
    public boolean isRoundedNECorner() { return encoding == TerrainTile.ARC_NE.$; }

    public boolean isAngularNWCorner() { return encoding == TerrainTile.ANG_ARC_NW.$; }
    public boolean isAngularSWCorner() { return encoding == TerrainTile.ANG_ARC_SW.$; }
    public boolean isAngularSECorner() { return encoding == TerrainTile.ANG_ARC_SE.$;}
    public boolean isAngularNECorner() { return encoding == TerrainTile.ANG_ARC_NE.$; }

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

    public boolean isVerticalLine() { return encoding == TerrainTile.WALL_V.$; }
    public boolean isHorizontalLine() { return encoding == TerrainTile.WALL_H.$; }
    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;

import java.util.Objects;

import static de.amr.pacmanfx.lib.tilemap.TerrainTileSet.TileID.*;

public record ObstacleSegment(Vector2i startPoint, Vector2i vector, boolean ccw, byte encoding) {

    public ObstacleSegment {
        Objects.requireNonNull(startPoint);
        Objects.requireNonNull(vector);
    }

    public Vector2i endPoint() {
        return startPoint.plus(vector);
    }

    public boolean isRoundedNWCorner() {
        return encoding == TerrainTileSet.valueOf(ARC_NW);
    }

    public boolean isRoundedSWCorner() {
        return encoding == TerrainTileSet.valueOf(ARC_SW);
    }

    public boolean isRoundedSECorner() {
        return encoding == TerrainTileSet.valueOf(ARC_SE);
    }

    public boolean isRoundedNECorner() {
        return encoding == TerrainTileSet.valueOf(ARC_NE);
    }

    public boolean isRoundedCorner() {
        return isRoundedNWCorner() || isRoundedSWCorner() || isRoundedSECorner() || isRoundedNECorner();
    }

    public boolean isAngularNWCorner() {
        return encoding == TerrainTileSet.valueOf(DCORNER_NW);
    }

    public boolean isAngularSWCorner() {
        return encoding == TerrainTileSet.valueOf(DCORNER_SW);
    }

    public boolean isAngularSECorner() {
        return encoding == TerrainTileSet.valueOf(DCORNER_SE);
    }

    public boolean isAngularNECorner() {
        return encoding == TerrainTileSet.valueOf(DCORNER_NE);
    }

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

    public boolean isVerticalLine() { return encoding == TerrainTileSet.valueOf(WALL_V); }
    public boolean isHorizontalLine() { return encoding == TerrainTileSet.valueOf(WALL_H); }
    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

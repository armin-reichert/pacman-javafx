/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;

import java.util.Objects;

public record ObstacleSegment(Vector2i startPoint, Vector2i vector, boolean ccw, byte encoding) {

    public ObstacleSegment {
        Objects.requireNonNull(startPoint);
        Objects.requireNonNull(vector);
    }

    public Vector2i endPoint() {
        return startPoint.plus(vector);
    }

    public boolean isRoundedNWCorner() {
        return encoding == TerrainTiles.CORNER_NW;
    }

    public boolean isRoundedSWCorner() {
        return encoding == TerrainTiles.CORNER_SW;
    }

    public boolean isRoundedSECorner() {
        return encoding == TerrainTiles.CORNER_SE;
    }

    public boolean isRoundedNECorner() {
        return encoding == TerrainTiles.CORNER_NE;
    }

    public boolean isRoundedCorner() {
        return isRoundedNWCorner() || isRoundedSWCorner() || isRoundedSECorner() || isRoundedNECorner();
    }

    public boolean isAngularNWCorner() {
        return encoding == TerrainTiles.DCORNER_ANGULAR_NW;
    }

    public boolean isAngularSWCorner() {
        return encoding == TerrainTiles.DCORNER_ANGULAR_SW;
    }

    public boolean isAngularSECorner() {
        return encoding == TerrainTiles.DCORNER_ANGULAR_SE;
    }

    public boolean isAngularNECorner() {
        return encoding == TerrainTiles.DCORNER_ANGULAR_NE;
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

    public boolean isVerticalLine() { return encoding == TerrainTiles.WALL_V; }

    public boolean isHorizontalLine() { return encoding == TerrainTiles.WALL_H; }

    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

public record ObstacleSegment(Vector2f start, Vector2f vector, boolean ccw, byte mapContent) {

    public Vector2f end() {
        return start.plus(vector);
    }

    public boolean isRoundedNWCorner() {
        return mapContent == TileEncoding.CORNER_NW || mapContent == TileEncoding.DCORNER_NW;
    }

    public boolean isRoundedSWCorner() {
        return mapContent == TileEncoding.CORNER_SW || mapContent == TileEncoding.DCORNER_SW;
    }

    public boolean isRoundedSECorner() {
        return mapContent == TileEncoding.CORNER_SE || mapContent == TileEncoding.DCORNER_SE;
    }

    public boolean isRoundedNECorner() {
        return mapContent == TileEncoding.CORNER_NE || mapContent == TileEncoding.DCORNER_NE;
    }

    public boolean isRoundedCorner() {
        return isRoundedNWCorner() || isRoundedSWCorner() || isRoundedSECorner() || isRoundedNECorner();
    }

    public boolean isAngularNWCorner() {
        return mapContent == TileEncoding.DCORNER_ANGULAR_NW;
    }

    public boolean isAngularSWCorner() {
        return mapContent == TileEncoding.DCORNER_ANGULAR_SW;
    }

    public boolean isAngularSECorner() {
        return mapContent == TileEncoding.DCORNER_ANGULAR_SE;
    }

    public boolean isAngularNECorner() {
        return mapContent == TileEncoding.DCORNER_ANGULAR_NE;
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

    public boolean isVerticalLine() {
        return mapContent == TileEncoding.WALL_V || mapContent == TileEncoding.DWALL_V;
    }

    public boolean isHorizontalLine() {
        return mapContent == TileEncoding.WALL_H || mapContent == TileEncoding.DWALL_H;
    }

    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

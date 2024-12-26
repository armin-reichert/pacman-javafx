/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

public record ObstacleSegment(Vector2f vector, boolean ccw, byte mapContent) {
    public boolean isRoundedNWCorner() {
        return mapContent == Tiles.CORNER_NW || mapContent == Tiles.DCORNER_NW;
    }

    public boolean isRoundedSWCorner() {
        return mapContent == Tiles.CORNER_SW || mapContent == Tiles.DCORNER_SW;
    }

    public boolean isRoundedSECorner() {
        return mapContent == Tiles.CORNER_SE || mapContent == Tiles.DCORNER_SE;
    }

    public boolean isRoundedNECorner() {
        return mapContent == Tiles.CORNER_NE || mapContent == Tiles.DCORNER_NE;
    }

    public boolean isRoundedCorner() {
        return isRoundedNWCorner() || isRoundedSWCorner() || isRoundedSECorner() || isRoundedNECorner();
    }

    public boolean isAngularNWCorner() {
        return mapContent == Tiles.DCORNER_ANGULAR_NW;
    }

    public boolean isAngularSWCorner() {
        return mapContent == Tiles.DCORNER_ANGULAR_SW;
    }

    public boolean isAngularSECorner() {
        return mapContent == Tiles.DCORNER_ANGULAR_SE;
    }

    public boolean isAngularNECorner() {
        return mapContent == Tiles.DCORNER_ANGULAR_NE;
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
        return mapContent == Tiles.WALL_V || mapContent == Tiles.DWALL_V;
    }

    public boolean isHorizontalLine() {
        return mapContent == Tiles.WALL_H || mapContent == Tiles.DWALL_H;
    }

    public boolean isStraightLine() {
        return isVerticalLine() || isHorizontalLine();
    }
}

/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    public record Segment(Vector2f vector, boolean ccw, byte mapContent) {
        public boolean isRoundedNWCorner() { return mapContent == Tiles.CORNER_NW || mapContent == Tiles.DCORNER_NW; }
        public boolean isRoundedSWCorner() { return mapContent == Tiles.CORNER_SW || mapContent == Tiles.DCORNER_SW; }
        public boolean isRoundedSECorner() { return mapContent == Tiles.CORNER_SE || mapContent == Tiles.DCORNER_SE; }
        public boolean isRoundedNECorner() { return mapContent == Tiles.CORNER_NE || mapContent == Tiles.DCORNER_NE; }
        public boolean isRoundedCorner() { return isRoundedNWCorner() || isRoundedSWCorner() || isRoundedSECorner() || isRoundedNECorner(); }
        public boolean isAngularNWCorner() { return mapContent == Tiles.DCORNER_ANGULAR_NW; }
        public boolean isAngularSWCorner() { return mapContent == Tiles.DCORNER_ANGULAR_SW; }
        public boolean isAngularSECorner() { return mapContent == Tiles.DCORNER_ANGULAR_SE; }
        public boolean isAngularNECorner() { return mapContent == Tiles.DCORNER_ANGULAR_NE; }
        public boolean isNWCorner() { return isRoundedNWCorner() || isAngularNWCorner(); }
        public boolean isSWCorner() { return isRoundedSWCorner() || isAngularSWCorner(); }
        public boolean isSECorner() { return isRoundedSECorner() || isAngularSECorner();}
        public boolean isNECorner() { return isRoundedNECorner() || isAngularNECorner(); }
        public boolean isVerticalLine()    {  return mapContent == Tiles.WALL_V || mapContent == Tiles.DWALL_V; }
        public boolean isHorizontalLine()  { return mapContent == Tiles.WALL_H || mapContent == Tiles.DWALL_H; }
        public boolean isStraightLine()    { return isVerticalLine() || isHorizontalLine(); }
    }

    private final List<Segment> segments = new ArrayList<>();
    private final Vector2f startPoint;
    private Vector2f endPoint;
    private final boolean doubleWalls;

    public Obstacle(Vector2f startPoint, boolean doubleWalls) {
        this.startPoint = startPoint;
        endPoint = startPoint;
        this.doubleWalls = doubleWalls;
    }

    public Vector2f[] points() {
        Vector2f[] points = new Vector2f[numSegments()];
        points[0] = startPoint;
        for (int i = 0; i < numSegments() - 1; ++i) {
            points[i+1] = points[i].plus(segment(i).vector());
        }
        return points;
    }

    @Override
    public String toString() {
        return "Obstacle{" +
            "startPoint=" + startPoint +
            ", endPoint=" + endPoint +
            ", segment count=" + segments.size() +
            ", segments=" + segments +
            '}';
    }

    public void addSegment(Vector2f vector, boolean ccw, byte content) {
        segments.add(new Segment(vector, ccw, content));
        endPoint = endPoint.plus(vector);
    }

    public Vector2f startPoint() {
        return startPoint;
    }

    public boolean isClosed() {
        return startPoint.equals(endPoint);
    }

    public boolean hasDoubleWalls() {
        return doubleWalls;
    }

    public List<Segment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public int numSegments() {
        return segments.size();
    }

    public Segment segment(int i) {
        return segments.get(i);
    }

    public boolean isUnitCircle() {
        return isClosed() && numSegments() == 4;
    }

    public boolean isO_Shape() {
        return isClosed() &&
            segments().stream().filter(Segment::isRoundedCorner).count() == 4;
    }

    public boolean isL_Shape() {
        return false; // TODO
    }

    public boolean isT_Shape() {
        return false; //TODO
    }
}
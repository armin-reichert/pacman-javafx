/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    //TODO better store only one of these?
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private final List<Vector2f> points = new ArrayList<>();
    private final boolean doubleWalls;

    public Obstacle(Vector2f startPoint, boolean doubleWalls) {
        points.add(startPoint);
        this.doubleWalls = doubleWalls;
    }

    public Vector2f[] points() {
        return points.toArray(Vector2f[]::new);
    }

    @Override
    public String toString() {
        return "Obstacle{" +
            "startPoint=" + points.getFirst() +
            ", endPoint=" + points.getLast() +
            ", segment count=" + segments.size() +
            ", segments=" + segments +
            '}';
    }

    public void addSegment(Vector2f vector, boolean ccw, byte content) {
        segments.add(new ObstacleSegment(vector, ccw, content));
        points.add(endPoint().plus(vector));
    }

    public Vector2f startPoint() { return points.getFirst(); }

    public Vector2f endPoint() { return points.getLast(); }

    public Vector2f point(int i) {
        return points.get(i);
    }

    public boolean isClosed() {
        return startPoint().equals(endPoint());
    }

    public boolean hasDoubleWalls() {
        return doubleWalls;
    }

    public List<ObstacleSegment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public int numSegments() {
        return segments.size();
    }

    public ObstacleSegment segment(int i) {
        return segments.get(i);
    }

    public boolean isO_Shape() {
        return isClosed() && segments().stream().filter(ObstacleSegment::isRoundedCorner).count() == 4;
    }

    public boolean isL_Shape() {
        return isClosed() && numSegments() == 10 && numDeadEnds() == 2;
    }

    public boolean isT_Shape() {
        //TODO: this is not 100% correct
        return isClosed() && numSegments() == 13 && numDeadEnds() == 3;
    }

    public boolean isCrossShape() {
        if (isClosed() && numSegments() == 20 && numDeadEnds() == 4) {
            //TODO Check if this is not an H-shape
            int[] d = deadEndSegmentIndices();
            Vector2f[] c = new Vector2f[4];
            for (int i = 0; i < 4; ++i) {
                c[i] = deadEndCenter(d[i]);
            }
            // d[0] = left, d[1] = bottom, d[2] = right, d[3] = top
            return c[0].x() < c[2].x() && c[0].y() == c[2].y() && c[3].y() < c[1].y() && c[3].x() == c[1].x();
        }
        return false;
    }

    public boolean isU_Shape() {
        int[] deadEnds = deadEndSegmentIndices();
        return numSegments() == 14 && deadEnds.length == 2
            && ( hAligned(points.get(deadEnds[0]), points.get(deadEnds[1]))
              || vAligned(points.get(deadEnds[0]), points.get(deadEnds[1])) );
    }

    private boolean hAligned(Vector2f p, Vector2f q) {
        return p.y() == q.y();
    }

    private boolean vAligned(Vector2f p, Vector2f q) {
        return p.x() == q.x();
    }

    public int[] deadEndSegmentIndices() {
        return IntStream.range(0, segments.size()).filter(this::hasDeadEndAt).toArray();
    }

    public int numDeadEnds() {
        return (int) IntStream.range(0, segments.size()).filter(this::hasDeadEndAt).count();
    }

    private boolean hasDeadEndAt(int i) {
        ObstacleSegment segment = segments.get(i);
        if (i < segments.size() - 1) {
            return segment.isRoundedCorner() && segments.get(i+1).isRoundedCorner();
        } else {
            return segment.isRoundedCorner() && segments.getFirst().isRoundedCorner();
        }
    }

    public Vector2f deadEndCenter(int index) {
        ObstacleSegment segment = segment(index);
        return switch (segment.mapContent()) {
            case Tiles.CORNER_NW -> points.get(index).plus(0, HTS);
            case Tiles.CORNER_SW -> points.get(index).plus(HTS, 0);
            case Tiles.CORNER_SE -> points.get(index).plus(0, -HTS);
            case Tiles.CORNER_NE -> points.get(index).plus(-HTS, 0);
            default -> throw new IllegalStateException();
        };
    }
}
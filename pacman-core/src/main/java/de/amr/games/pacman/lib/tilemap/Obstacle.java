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

    public ObstacleType computeType() {
        if (!isClosed()) {
            return ObstacleType.ANY;
        }
        if (segments().stream().filter(ObstacleSegment::isRoundedCorner).count() == 4) {
            return ObstacleType.O_SHAPE;
        }
        int[] d = deadEndSegmentIndices();
        Vector2f[] c = deadEndCenters();
        if (d.length == 2 && (numSegments() == 9 || numSegments() == 10)) {
            return ObstacleType.L_SHAPE;
        }
        if (d.length == 2 && numSegments() == 14) {
            if (hAligned(c[0], c[1]) || vAligned(c[0], c[1]) ) {
                return ObstacleType.U_SHAPE;
            } else {
                return ObstacleType.S_SHAPE; // TODO correct?
            }
        }
        if (d.length == 3 && numSegments() == 13) {
            return ObstacleType.T_SHAPE;
        }
        if (d.length == 4 && numSegments() == 20) {
            // Check if this is not an H-shape
            // d[0] = left, d[1] = bottom, d[2] = right, d[3] = top
            if (c[0].x() < c[2].x() && c[0].y() == c[2].y() &&
                    c[3].y() < c[1].y() && c[3].x() == c[1].x()) {
                return ObstacleType.CROSS_SHAPE;
            }
        }
        return ObstacleType.ANY;
    }

    public Vector2f[] deadEndCenters() {
        return deadEndCenters(deadEndSegmentIndices());
    }

    public Vector2f[] deadEndCenters(int[] indices) {
        Vector2f[] c = new Vector2f[indices.length];
        for (int i = 0; i < indices.length; ++i) {
            c[i] = towerCenterPoint(indices[i]);
        }
        return c;
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
        ObstacleSegment nextSegment = i < segments.size() - 1 ? segments.get(i + 1) : segments.getFirst();
        return segmentsFormDeadEnd(segment, nextSegment);
    }

    /** Tells if two segments in counter-clockwise order form a dead-end */
    private boolean segmentsFormDeadEnd(ObstacleSegment s1, ObstacleSegment s2) {
        if (s1.isRoundedNECorner() && s2.isRoundedNWCorner()) return true;
        if (s1.isRoundedNWCorner() && s2.isRoundedSWCorner()) return true;
        if (s1.isRoundedSWCorner() && s2.isRoundedSECorner()) return true;
        if (s1.isRoundedSECorner() && s2.isRoundedNECorner()) return true;
        if (s1.isAngularNECorner() && s2.isAngularNWCorner()) return true;
        if (s1.isAngularNWCorner() && s2.isAngularSWCorner()) return true;
        if (s1.isAngularSWCorner() && s2.isAngularSECorner()) return true;
        if (s1.isAngularSECorner() && s2.isAngularNECorner()) return true;
        return false;
    }

    public Vector2f towerCenterPoint(int index) {
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
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;

import java.util.*;
import java.util.stream.IntStream;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    private final Vector2i startPoint;
    private Vector2i endPoint;
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private final boolean doubleWalls;

    public Obstacle(Vector2i startPoint, boolean doubleWalls) {
        this.startPoint = this.endPoint = Objects.requireNonNull(startPoint);
        this.doubleWalls = doubleWalls;
    }

    public void addSegment(Vector2i vector, boolean ccw, byte content) {
        ObstacleSegment segment = new ObstacleSegment(endPoint, vector, ccw, content);
        segments.add(segment);
        endPoint = segment.endPoint();
    }

    public Vector2i[] points() {
        List<Vector2i> points = new ArrayList<>();
        points.add(startPoint);
        for (ObstacleSegment segment : segments) {
            points.add(segment.startPoint().plus(segment.vector()));
        }
        return points.toArray(Vector2i[]::new);
    }

    @Override
    public String toString() {
        return "Obstacle{" +
            "encoding=" + encoding() +
            ", start=" + startPoint +
            ", end=" + endPoint +
            ", segment count=" + segments.size() +
            ", segments=" + segments +
            '}';
    }

    public String encoding() {
        StringBuilder encoding = new StringBuilder();
        for (int i = 0; i < segments.size(); ++i) {
            byte tileCode = segment(i).encoding();
            char ch = (char) ('a' + tileCode);
            encoding.append(ch);
        }
        return encoding.toString();
    }

    public Vector2i startPoint() { return startPoint; }

    public Vector2i endPoint() { return endPoint; }

    public Vector2i point(int i) {
        return i < numSegments() ? segment(i).startPoint() : segment(i).endPoint();
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

    public IntStream uTurnSegmentIndices() {
        return IntStream.range(0, segments.size()).filter(this::hasUTurnAt);
    }

    private boolean hasUTurnAt(int i) {
        ObstacleSegment segment = segments.get(i);
        ObstacleSegment nextSegment = i < segments.size() - 1 ? segments.get(i + 1) : segments.getFirst();
        return isUTurn(segment, nextSegment);
    }

    /** Tells if two corner segments in counter-clockwise order form a U-turn */
    private boolean isUTurn(ObstacleSegment s1, ObstacleSegment s2) {
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

    public Vector2i cornerCenter(int segmentIndex) {
        ObstacleSegment corner = segment(segmentIndex);
        return switch (corner.encoding()) {
            case TileEncoding.CORNER_NW -> point(segmentIndex).plus(0, HTS);
            case TileEncoding.CORNER_SW -> point(segmentIndex).plus(HTS, 0);
            case TileEncoding.CORNER_SE -> point(segmentIndex).plus(0, -HTS);
            case TileEncoding.CORNER_NE -> point(segmentIndex).plus(-HTS, 0);
            default -> throw new IllegalStateException("No corner tile at index " + segmentIndex);
        };
    }

    public Vector2i[] cornerCentersAtSegments(int... segmentIndices) {
        return Arrays.stream(segmentIndices).mapToObj(this::cornerCenter).toArray(Vector2i[]::new);
    }

    public Vector2i[] cornerCenters() {
        var centers = new LinkedHashSet<Vector2i>();
        for (var segment : segments) {
            boolean up = segment.vector().y() < 0, down = segment.vector().y() > 0;
            switch (segment.encoding()) {
                case TileEncoding.CORNER_NW, TileEncoding.DCORNER_NW -> {
                    if (down) centers.add(segment.startPoint().plus(0, HTS));
                }
                case TileEncoding.CORNER_SW, TileEncoding.DCORNER_SW -> {
                    if (down) centers.add(segment.startPoint().plus(HTS, 0));
                }
                case TileEncoding.CORNER_SE, TileEncoding.DCORNER_SE -> {
                    if (up) centers.add(segment.startPoint().plus(0, -HTS));
                }
                case TileEncoding.CORNER_NE, TileEncoding.DCORNER_NE -> {
                    if (up) centers.add(segment.startPoint().plus(-HTS, 0));
                }
            }
        }
        return centers.toArray(Vector2i[]::new);
    }
}
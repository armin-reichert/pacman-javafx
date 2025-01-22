/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

import java.util.*;
import java.util.stream.IntStream;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    private final Vector2f startPoint;
    private Vector2f endPoint;
    private final List<ObstacleSegment> segments = new ArrayList<>();
    private final boolean doubleWalls;

    public Obstacle(Vector2f startPoint, boolean doubleWalls) {
        this.startPoint = this.endPoint = Objects.requireNonNull(startPoint);
        this.doubleWalls = doubleWalls;
    }

    public Vector2f[] points() {
        List<Vector2f> points = new ArrayList<>();
        points.add(startPoint);
        for (ObstacleSegment segment : segments) {
            points.add(segment.start().plus(segment.vector()));
        }
        return points.toArray(Vector2f[]::new);
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
            byte tileCode = segment(i).mapContent();
            char ch = (char) ('a' + tileCode);
            encoding.append(ch);
        }
        return encoding.toString();
    }

    public void addSegment(Vector2f vector, boolean ccw, byte content) {
        if (segments.isEmpty()) {
            segments.add(new ObstacleSegment(startPoint, vector, ccw, content));
        } else {
            segments.add(new ObstacleSegment(segments.getLast().end(), vector, ccw, content));
        }
        endPoint = segments.getLast().end();
    }

    public Vector2f startPoint() { return startPoint; }

    public Vector2f endPoint() { return endPoint; }

    public Vector2f point(int i) {
        return i < numSegments() ? segment(i).start() : segment(i).end();
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
        String encoding = encoding();
        return Arrays.stream(ObstacleType.values())
                .filter(type -> type.matches(encoding)).findFirst().orElse(ObstacleType.ANY);
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

    public Vector2f cornerCenter(int segmentIndex) {
        ObstacleSegment corner = segment(segmentIndex);
        return switch (corner.mapContent()) {
            case TileEncoding.CORNER_NW -> point(segmentIndex).plus(0, HTS);
            case TileEncoding.CORNER_SW -> point(segmentIndex).plus(HTS, 0);
            case TileEncoding.CORNER_SE -> point(segmentIndex).plus(0, -HTS);
            case TileEncoding.CORNER_NE -> point(segmentIndex).plus(-HTS, 0);
            default -> throw new IllegalStateException("No corner tile at index " + segmentIndex);
        };
    }

    public Vector2f[] cornerCenters(int... segmentIndices) {
        return Arrays.stream(segmentIndices).mapToObj(this::cornerCenter).toArray(Vector2f[]::new);
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    private final List<ObstacleSegment> segments = new ArrayList<>();
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
        segments.add(new ObstacleSegment(vector, ccw, content));
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
            //TODO Test if this is not an H-shape
            List<Integer> deadEnds = deadEndSegmentPositions();
            Vector2f[] points = points();
            Vector2f[] d = new Vector2f[4];
            for (int i = 0; i < 4; ++i) {
                d[i] = points[deadEnds.get(i)];
            }
            return true;
        }
        return false;
    }

    public boolean isU_Shape() {
        List<Integer> deadEnds = deadEndSegmentPositions();
        Vector2f[] points = points();
        return numSegments() == 14 && deadEnds.size() == 2
            && ( hAligned(points[deadEnds.getFirst()], points[deadEnds.getLast()])
              || vAligned(points[deadEnds.getFirst()], points[deadEnds.getLast()]) );
    }

    private boolean hAligned(Vector2f p, Vector2f q) {
        return p.y() == q.y();
    }

    private boolean vAligned(Vector2f p, Vector2f q) {
        return p.x() == q.x();
    }

    public List<Integer> deadEndSegmentPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < segments.size(); ++i) {
            if (hasDeadEndAt(i)) {
                positions.add(i);
            }
        }
        return positions;
    }

    public int numDeadEnds() {
        int count = 0;
        for (int i = 0; i < segments.size(); ++i) {
            if (hasDeadEndAt(i)) {
                count += 1;
            }
        }
        return count;
    }

    private boolean hasDeadEndAt(int i) {
        ObstacleSegment segment = segments.get(i);
        if (i < segments.size() - 1) {
            return segment.isRoundedCorner() && segments.get(i+1).isRoundedCorner();
        } else {
            return segment.isRoundedCorner() && segments.getFirst().isRoundedCorner();
        }
    }

    public Vector2f deadEndCenter(Obstacle obstacle, Vector2f[] points, int index) {
        ObstacleSegment segment = obstacle.segment(index);
        return switch (segment.mapContent()) {
            case Tiles.CORNER_NW -> points[index].plus(0, HTS);
            case Tiles.CORNER_SW -> points[index].plus(HTS, 0);
            case Tiles.CORNER_SE -> points[index].plus(0, -HTS);
            case Tiles.CORNER_NE -> points[index].plus(-HTS, 0);
            default -> throw new IllegalStateException();
        };
    }

}
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

    public boolean isUnitCircle() {
        return isClosed() && numSegments() == 4;
    }

    public boolean isO_Shape() {
        return isClosed() &&
            segments().stream().filter(ObstacleSegment::isRoundedCorner).count() == 4;
    }

    public boolean isL_Shape() {
        return false; // TODO
    }

    public boolean isT_Shape() {
        return false; //TODO
    }
}
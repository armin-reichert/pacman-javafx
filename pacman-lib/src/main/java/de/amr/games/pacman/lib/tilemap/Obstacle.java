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

    public record Segment(Vector2f vector, boolean ccw, byte mapContent) {}

    private final List<Segment> segments = new ArrayList<>();
    private final Vector2f startPoint;
    private Vector2f endPoint;

    public Obstacle(Vector2f startPoint) {
        this.startPoint = startPoint;
        endPoint = startPoint;
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
        return startPoint.equals(endPoint); // TODO use almost equals?
    }

    public List<Segment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public Segment segment(int i) {
        return segments.get(i);
    }
}
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Obstacle {

    private final Vector2f startPoint;
    private final List<Vector2f> segments = new ArrayList<>();
    private Vector2f endPoint;

    public Obstacle(Vector2f startPoint) {
        this.startPoint = startPoint;
        endPoint = startPoint;
    }

    public void addSegment(Vector2f segment) {
        segments.add(segment);
        endPoint = endPoint.plus(segment);
    }

    public Vector2f startPoint() {
        return startPoint;
    }

    public boolean isClosed() {
        return startPoint.equals(endPoint); // TODO use almost equals?
    }

    public List<Vector2f> segments() {
        return Collections.unmodifiableList(segments);
    }
}

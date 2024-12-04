/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Open or closed polygon.
 */
public class Obstacle {

    private final Vector2f startPoint;
    private final List<Vector2f> segments = new ArrayList<>();
    private final List<Direction> orientations = new ArrayList<>();
    private final List<Byte> contents = new ArrayList<>();
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

    public void addSegment(Vector2f segment, Direction orientation, byte content) {
        segments.add(segment);
        orientations.add(orientation);
        contents.add(content);
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

    public Vector2f segment(int i) {
        return segments.get(i);
    }

    public byte content(int i) {
        return contents.get(i);
    }

    public Direction orientation(int i) {
        return orientations.get(i);
    }
}

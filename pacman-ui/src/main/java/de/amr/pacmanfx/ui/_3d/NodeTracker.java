/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class NodeTracker {
    private static final int ANGLE_TOWARDS_VIEWER = -90;

    private final AnimationTimer timer;
    private final Node observer;
    private Node target;

    public NodeTracker(Node observer) {
        this.observer = requireNonNull(observer);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateObserverRotation();
            }
        };
    }

    public void startTracking(Node target) {
        this.target = requireNonNull(target);
        timer.start();
    }

    public void stopTracking() {
        timer.stop();
    }

    private void updateObserverRotation() {
        if (target == null) return;
        Point2D targetPos = target.localToScene(Point2D.ZERO);
        Point2D observerPos = observer.localToScene(Point2D.ZERO);
        Point2D arrow = (targetPos.subtract(observerPos)).normalize();
        double phi = Math.toDegrees(Math.atan2(arrow.getX(), arrow.getY()));
        double rotate = ANGLE_TOWARDS_VIEWER - phi;
        observer.setRotationAxis(Rotate.Z_AXIS);
        observer.setRotate(rotate);
    }
}

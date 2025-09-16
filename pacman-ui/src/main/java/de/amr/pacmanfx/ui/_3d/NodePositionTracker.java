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

/**
 * Keeps the rotation of an observer node always directed towards the current location of an observed node. Used to let
 * the Pac-Man shapes in the life counter keep looking at the current Pac-Man position.
 */
public class NodePositionTracker {
    private static final float SMOOTHING_FACTOR = 0.2f;
    private static final int ANGLE_TOWARDS_VIEWER = -90;

    private final AnimationTimer timer;
    private final Node observer;
    private Node target;
    private double currentAngle;

    public NodePositionTracker(Node observer) {
        this.observer = requireNonNull(observer);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (target != null) {
                    rotateTowardsTarget();
                }
            }
        };
    }

    public void startTrackingTarget(Node target) {
        this.target = requireNonNull(target);
        timer.start();
    }

    public void stopTracking() {
        timer.stop();
    }

    private void rotateTowardsTarget() {
        Point2D observerPos = observer.localToScene(Point2D.ZERO);
        Point2D targetPos = target.localToScene(Point2D.ZERO);
        Point2D dirVector = (targetPos.subtract(observerPos)).normalize();
        double phi = Math.toDegrees(Math.atan2(dirVector.getX(), dirVector.getY()));
        double angle = ANGLE_TOWARDS_VIEWER - phi;
        currentAngle += (angle - currentAngle) * SMOOTHING_FACTOR;
        observer.setRotationAxis(Rotate.Z_AXIS);
        observer.setRotate(angle);
    }
}
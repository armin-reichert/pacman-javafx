/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector2f;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.HTS;

/**
 * Oszillation animation for pellets (unused).
 */
public class FoodOscillation extends Transition {

    private static final Vector2f CENTER = Vector2f.of(28 * HTS, 36 * HTS);

    private final Group foodGroup;

    public FoodOscillation(Group foodGroup) {
        this.foodGroup = foodGroup;
        setCycleDuration(Duration.seconds(0.6));
        setCycleCount(INDEFINITE);
        setAutoReverse(true);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double t) {
        for (var node : foodGroup.getChildren()) {
            var position2D = new Vector2f((float) node.getTranslateX(), (float) node.getTranslateY());
            var centerDistance = position2D.euclideanDist(CENTER);
            double dz = 2 * Math.sin(2 * centerDistance) * t;
            node.setTranslateZ(-4 + dz);
        }
    }
}
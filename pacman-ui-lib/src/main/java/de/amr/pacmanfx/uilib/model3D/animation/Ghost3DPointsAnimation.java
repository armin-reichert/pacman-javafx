/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.GhostAppearance3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Ghost3DPointsAnimation extends ManagedAnimation {

    private final GhostAppearance3D ghostAppearance3D;

    public Ghost3DPointsAnimation( GhostAppearance3D ghostAppearance3D) {
        super("Ghost Points (%s)".formatted(ghostAppearance3D.ghost().name()));
        this.ghostAppearance3D = requireNonNull(ghostAppearance3D);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        final Optional<Shape3D> optNumberShape = ghostAppearance3D.optNumberShape3D();
        if (optNumberShape.isPresent()) {
            var numberBoxRotation = new RotateTransition(Duration.seconds(1), optNumberShape.get());
            numberBoxRotation.setAxis(Rotate.X_AXIS);
            numberBoxRotation.setFromAngle(0);
            numberBoxRotation.setToAngle(360);
            numberBoxRotation.setInterpolator(Interpolator.LINEAR);
            numberBoxRotation.setRate(0.75);
            return numberBoxRotation;
        }
        else {
            throw new IllegalStateException("No number shape 3D found!");
        }
    }
}

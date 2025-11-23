/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ColorChangeTransition extends Transition {

    private final Color start;
    private final Color end;
    private final ObjectProperty<Color> clientColorPy;

    public ColorChangeTransition(Duration duration, Color start, Color end, ObjectProperty<Color> clientColorPy) {
        requireNonNull(duration);
        requireNonNull(start);
        requireNonNull(end);
        requireNonNull(clientColorPy);
        this.start = start;
        this.end = end;
        this.clientColorPy = clientColorPy;
        setCycleCount(1);
        setCycleDuration(duration);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double t) {
        clientColorPy.setValue(start.interpolate(end, t));
    }
}
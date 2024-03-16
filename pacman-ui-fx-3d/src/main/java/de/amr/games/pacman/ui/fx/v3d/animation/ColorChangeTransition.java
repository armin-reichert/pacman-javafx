/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class ColorChangeTransition extends Transition {

    private final Color start;
    private final Color end;
    private final ObjectProperty<Color> clientColorPy;

    public ColorChangeTransition(Duration duration, Color start, Color end, ObjectProperty<Color> clientColorPy) {
        checkNotNull(duration);
        checkNotNull(start);
        checkNotNull(end);
        checkNotNull(clientColorPy);
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
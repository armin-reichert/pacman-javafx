/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.animation;

import de.amr.games.pacman.lib.Globals;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ColorChangeTransition extends Transition {

    private final Color start;
    private final Color end;
    private final ObjectProperty<Color> clientColorPy;

    public ColorChangeTransition(Duration duration, Color start, Color end, ObjectProperty<Color> clientColorPy) {
        Globals.assertNotNull(duration);
        Globals.assertNotNull(start);
        Globals.assertNotNull(end);
        Globals.assertNotNull(clientColorPy);
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
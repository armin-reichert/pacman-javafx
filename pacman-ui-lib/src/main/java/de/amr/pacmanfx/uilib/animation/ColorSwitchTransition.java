/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ColorSwitchTransition extends Transition {

    public final ObjectProperty<Color> colorPy;

    private final Color startColor;
    private final Color endColor;

    /**
     * A transition that changes the color property permanently between to given color values. The number of switches
     * during an interval is specified by the parameters totalSeconds and numSwitches.
     *
     * @param startColor color with which the switching animation starts
     * @param endColor color to which the transition changes
     * @param intervalInSeconds duration of <code>numSwitches</code> repetitions
     * @param numSwitches switches made in total seconds interval
     */
    public ColorSwitchTransition(Color startColor, Color endColor, double intervalInSeconds, int numSwitches) {
        requireNonNull(startColor);
        requireNonNull(endColor);
        if (intervalInSeconds < 0) {
            throw new IllegalArgumentException("Interval duration must be non-negative but is " + intervalInSeconds);
        }
        if (numSwitches <= 0) {
            throw new IllegalArgumentException("Number of switches must be positive but is " + numSwitches);
        }
        this.startColor = startColor;
        this.endColor = endColor;
        colorPy = new SimpleObjectProperty<>(startColor);
        setCycleCount(INDEFINITE);
        setCycleDuration(Duration.seconds(intervalInSeconds / numSwitches));
        setAutoReverse(true);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double t) {
        colorPy.set(startColor.interpolate(endColor, t));
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * A simple color interpolation animation that transitions smoothly from a
 * start color to an end color over a specified duration.
 *
 * <p>This transition does not animate a node directly. Instead, it writes the
 * interpolated color into a caller-provided {@link ObjectProperty} of type
 * {@link Color}. This makes the transition flexible and suitable for animating
 * any color property, such as:
 *
 * <ul>
 *   <li>fill or stroke colors</li>
 *   <li>material or lighting colors</li>
 *   <li>custom UI component color properties</li>
 * </ul>
 *
 * <p>The interpolation is linear and uses {@link Color#interpolate(Color, double)}
 * to ensure correct blending in JavaFX color space.</p>
 */
public class ColorChangeTransition extends Transition {

    private final Color startColor;
    private final Color endColor;
    private final ObjectProperty<Color> targetProperty;

    /**
     * Creates a new color transition.
     *
     * @param duration       the total duration of the transition (must not be null)
     * @param startColor     the initial color at time {@code t = 0} (must not be null)
     * @param endColor       the final color at time {@code t = 1} (must not be null)
     * @param targetProperty the property that receives the interpolated color
     *                       during the animation (must not be null)
     */
    public ColorChangeTransition(
        Duration duration,
        Color startColor,
        Color endColor,
        ObjectProperty<Color> targetProperty) {

        requireNonNull(duration, "duration must not be null");
        requireNonNull(startColor, "startColor must not be null");
        requireNonNull(endColor, "endColor must not be null");
        requireNonNull(targetProperty, "targetProperty must not be null");

        this.startColor = startColor;
        this.endColor = endColor;
        this.targetProperty = targetProperty;

        setCycleDuration(duration);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double t) {
        targetProperty.set(startColor.interpolate(endColor, t));
    }
}

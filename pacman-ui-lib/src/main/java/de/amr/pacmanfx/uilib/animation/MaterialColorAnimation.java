/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

public class MaterialColorAnimation extends Transition {

    private final PhongMaterial material;
    private final Color fromColor;
    private final Color toColor;

    public MaterialColorAnimation(Duration cycleDuration, PhongMaterial material, Color fromColor, Color toColor) {
        this.material = material;
        this.fromColor = fromColor;
        this.toColor = toColor;
        setAutoReverse(true);
        setCycleCount(Animation.INDEFINITE);
        setCycleDuration(cycleDuration);
    }

    @Override
    protected void interpolate(double t) {
        Color color = fromColor.interpolate(toColor, t);
        material.setDiffuseColor(color);
        material.setSpecularColor(color.brighter());
    }
}

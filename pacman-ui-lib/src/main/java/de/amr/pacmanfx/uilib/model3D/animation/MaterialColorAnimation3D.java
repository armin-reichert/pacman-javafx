/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

public class MaterialColorAnimation3D {

    public static Animation create(double seconds, PhongMaterial material, Color fromColor, Color toColor) {
        return new Timeline(
            new KeyFrame(Duration.ZERO,  new KeyValue(material.diffuseColorProperty(), fromColor)),
            new KeyFrame(Duration.seconds(seconds), new KeyValue(material.diffuseColorProperty(), toColor))
        );
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;


import de.amr.pacmanfx.ui.config.ui.CommonSettings2D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class CommonSettings2DModel {

    public ObjectProperty<Color> canvasBackgroundColorProperty;

    public BooleanProperty fontSmoothingOnProperty;

    public CommonSettings2DModel() {
        canvasBackgroundColorProperty = new SimpleObjectProperty<>();
        fontSmoothingOnProperty = new SimpleBooleanProperty();
    }

    public void init(CommonSettings2D settings) {
        canvasBackgroundColorProperty.set(settings.canvasBackgroundColor());
        fontSmoothingOnProperty.set(settings.fontSmoothingOn());
    }
}

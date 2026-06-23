/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.viewmodel;


import de.amr.pacmanfx.ui.config.ui.UISettings2D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class UISettings2DVM {

    public ObjectProperty<Color> canvasBackgroundColorProperty;

    public BooleanProperty fontSmoothingOnProperty;

    public UISettings2DVM() {
        canvasBackgroundColorProperty = new SimpleObjectProperty<>();
        fontSmoothingOnProperty = new SimpleBooleanProperty();
    }

    public void init(UISettings2D settings) {
        canvasBackgroundColorProperty.set(settings.canvasBackgroundColor());
        fontSmoothingOnProperty.set(settings.fontSmoothingOn());
    }
}

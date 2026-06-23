/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.viewmodel;


import de.amr.pacmanfx.ui.config.ui.Settings2D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class Settings2DViewModel {

    public ObjectProperty<Color> canvasBackgroundColorProperty;

    public BooleanProperty fontSmoothingOnProperty;

    public Settings2DViewModel() {
        canvasBackgroundColorProperty = new SimpleObjectProperty<>();
        fontSmoothingOnProperty = new SimpleBooleanProperty();
    }

    public void init(Settings2D settings) {
        canvasBackgroundColorProperty.set(settings.canvasBackgroundColor());
        fontSmoothingOnProperty.set(settings.fontSmoothingOn());
    }
}

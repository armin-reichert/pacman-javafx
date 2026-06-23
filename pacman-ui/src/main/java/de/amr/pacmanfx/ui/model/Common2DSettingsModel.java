/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;


import de.amr.pacmanfx.ui.config.ui.Common2DSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class Common2DSettingsModel {

    public ObjectProperty<Color> canvasBackgroundColorProperty;

    public BooleanProperty fontSmoothingOnProperty;

    public Common2DSettingsModel() {
        canvasBackgroundColorProperty = new SimpleObjectProperty<>();
        fontSmoothingOnProperty = new SimpleBooleanProperty();
    }

    public void init(Common2DSettings settings) {
        canvasBackgroundColorProperty.set(settings.canvasBackgroundColor());
        fontSmoothingOnProperty.set(settings.fontSmoothingOn());
    }
}

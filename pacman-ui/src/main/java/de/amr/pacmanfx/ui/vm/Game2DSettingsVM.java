/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;


import de.amr.pacmanfx.ui.settings.ui.Game2DSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class Game2DSettingsVM {

    public ObjectProperty<Color> canvasBackgroundColorProperty;

    public BooleanProperty fontSmoothingOnProperty;

    public Game2DSettingsVM() {
        canvasBackgroundColorProperty = new SimpleObjectProperty<>();
        fontSmoothingOnProperty = new SimpleBooleanProperty();
    }

    public void init(Game2DSettings settings) {
        canvasBackgroundColorProperty.set(settings.canvasBackgroundColor());
        fontSmoothingOnProperty.set(settings.fontSmoothingOn());
    }
}

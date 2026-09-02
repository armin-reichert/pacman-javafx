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

    private final ObjectProperty<Color> canvasBackgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty fontSmoothingOn = new SimpleBooleanProperty(false);

    public Game2DSettingsVM() {}

    public void init(Game2DSettings settings) {
        canvasBackgroundColor.set(settings.canvasBackgroundColor());
        fontSmoothingOn.set(settings.fontSmoothingOn());
    }

    public ObjectProperty<Color> canvasBackgroundColorProperty() {
        return canvasBackgroundColor;
    }

    public BooleanProperty fontSmoothingOnProperty() {
        return fontSmoothingOn;
    }
}

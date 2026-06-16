package de.amr.pacmanfx.ui.game;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public record UISettings(
    Duration flashMessageDuration,
    ObjectProperty<Color> canvasBackgroundColorProperty,
    BooleanProperty fontSmoothingOnProperty,
    BooleanProperty debugModeOnProperty,
    BooleanProperty keyboardMonitorOnProperty,
    IntegerProperty miniViewHeightProperty,
    BooleanProperty miniViewOnProperty,
    IntegerProperty miniViewOpacityPercentProperty,
    BooleanProperty mutedProperty,
    IntegerProperty numSimulationStepsProperty)
{
    public UISettings() {
        this(
            Duration.seconds(1.5),
            new SimpleObjectProperty<>(Color.BLACK),
            new SimpleBooleanProperty(false),
            new SimpleBooleanProperty(false),
            new SimpleBooleanProperty(false),
            new SimpleIntegerProperty(400),
            new SimpleBooleanProperty(false),
            new SimpleIntegerProperty(69),
            new SimpleBooleanProperty(false),
            new SimpleIntegerProperty(1)
        );
    }
}

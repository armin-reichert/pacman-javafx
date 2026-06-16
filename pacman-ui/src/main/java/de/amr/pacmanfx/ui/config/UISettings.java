package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
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
    IntegerProperty numSimulationStepsProperty,
    UISettings3D d3)
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
            new SimpleIntegerProperty(1),
            new UISettings3D(
                new SimpleBooleanProperty(false),
                new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER),
                new SimpleObjectProperty<>(DrawMode.FILL),
                new SimpleBooleanProperty(false),
                new SimpleObjectProperty<>(Color.rgb(20, 20, 20)),
                new SimpleObjectProperty<>(Color.WHITE),
                new SimpleDoubleProperty(),
                new SimpleDoubleProperty(1.0)

            )
        );
    }
}

package de.amr.pacmanfx.ui.config;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public record UISettingsProperties(
    ObjectProperty<Duration> flashMessageDurationProperty,
    ObjectProperty<Color> canvasBackgroundColorProperty,
    BooleanProperty fontSmoothingOnProperty,
    BooleanProperty debugModeOnProperty,
    BooleanProperty keyboardMonitorOnProperty,
    BooleanProperty mutedProperty,
    IntegerProperty numSimulationStepsProperty,
    MiniViewSettingsProperties miniView,
    UISettings3DProperties d3)
{
    public UISettingsProperties(UISettings values) {
        this(
            new SimpleObjectProperty<>(Duration.seconds(values.flashMessageDuration())),
            new SimpleObjectProperty<>(values.canvasBackgroundColor()),
            new SimpleBooleanProperty(values.fontSmoothingOn()),
            new SimpleBooleanProperty(values.debugModeOn()),
            new SimpleBooleanProperty(values.keyboardMonitorOn()),
            new SimpleBooleanProperty(values.muted()),
            new SimpleIntegerProperty(values.numSimulationSteps()),
            new MiniViewSettingsProperties(
                new SimpleIntegerProperty(values.miniView().height()),
                new SimpleBooleanProperty(values.miniView().active()),
                new SimpleIntegerProperty(values.miniView().opacityPercentage())
            ),
            new UISettings3DProperties(
                new SimpleBooleanProperty(values.d3().axesVisible()),
                new SimpleObjectProperty<>(values.d3().cameraPerspectiveId()),
                new SimpleObjectProperty<>(values.d3().drawMode()),
                new SimpleBooleanProperty(values.d3().view3DEnabled()),
                new SimpleObjectProperty<>(values.d3().mazeFloorColor()),
                new SimpleObjectProperty<>(values.d3().mazeLightColor()),
                new SimpleDoubleProperty(values.d3().mazeWallHeight()),
                new SimpleDoubleProperty(values.d3().mazeWallOpacity())
            )
        );
    }
}

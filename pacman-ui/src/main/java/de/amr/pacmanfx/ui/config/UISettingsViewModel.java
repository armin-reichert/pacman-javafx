package de.amr.pacmanfx.ui.config;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class UISettingsViewModel {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final ObjectProperty<Color> canvasBackgroundColorProperty;
    public final BooleanProperty fontSmoothingOnProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewSettingsViewModel miniView;
    public final UISettings3DViewModel d3;

    public UISettingsViewModel(UISettings values) {

        flashMessageDurationProperty = new SimpleObjectProperty<>(Duration.seconds(values.flashMessageDuration()));
        canvasBackgroundColorProperty = new SimpleObjectProperty<>(values.canvasBackgroundColor());
        fontSmoothingOnProperty = new SimpleBooleanProperty(values.fontSmoothingOn());
        debugModeOnProperty = new SimpleBooleanProperty(values.debugModeOn());
        keyboardMonitorOnProperty = new SimpleBooleanProperty(values.keyboardMonitorOn());
        mutedProperty = new SimpleBooleanProperty(values.muted());
        numSimulationStepsProperty = new SimpleIntegerProperty(values.numSimulationSteps());

        miniView = new MiniViewSettingsViewModel(
            new SimpleIntegerProperty(values.miniView().height()),
            new SimpleBooleanProperty(values.miniView().active()),
            new SimpleIntegerProperty(values.miniView().opacityPercentage())
        );

        d3 = new UISettings3DViewModel(
            new SimpleBooleanProperty(values.d3().axesVisible()),
            new SimpleObjectProperty<>(values.d3().cameraPerspectiveId()),
            new SimpleObjectProperty<>(values.d3().drawMode()),
            new SimpleBooleanProperty(values.d3().view3DEnabled()),
            new SimpleObjectProperty<>(values.d3().mazeFloorColor()),
            new SimpleObjectProperty<>(values.d3().mazeLightColor()),
            new SimpleDoubleProperty(values.d3().mazeWallHeight()),
            new SimpleDoubleProperty(values.d3().mazeWallOpacity())
        );
    }
}


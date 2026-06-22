package de.amr.pacmanfx.ui.viewmodel;

import de.amr.pacmanfx.ui.config.UISettings;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * UI settings view model.
 */
public class UISettingsVM {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final ObjectProperty<Color> canvasBackgroundColorProperty;
    public final BooleanProperty fontSmoothingOnProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewVM miniView;
    public final UISettings3DVM d3;

    public UISettingsVM(UISettings settings) {

        flashMessageDurationProperty = new SimpleObjectProperty<>(Duration.seconds(settings.flashMessageDuration()));
        canvasBackgroundColorProperty = new SimpleObjectProperty<>(settings.canvasBackgroundColor());
        fontSmoothingOnProperty = new SimpleBooleanProperty(settings.fontSmoothingOn());
        debugModeOnProperty = new SimpleBooleanProperty(settings.debugModeOn());
        keyboardMonitorOnProperty = new SimpleBooleanProperty(settings.keyboardMonitorOn());
        mutedProperty = new SimpleBooleanProperty(settings.muted());
        numSimulationStepsProperty = new SimpleIntegerProperty(settings.numSimulationSteps());

        miniView = new MiniViewVM(
            new SimpleIntegerProperty(settings.miniView().height()),
            new SimpleBooleanProperty(settings.miniView().active()),
            new SimpleIntegerProperty(settings.miniView().opacityPercentage())
        );

        d3 = new UISettings3DVM(
            new SimpleBooleanProperty(settings.d3().axesVisible()),
            new SimpleObjectProperty<>(settings.d3().cameraPerspectiveId()),
            new SimpleObjectProperty<>(settings.d3().drawMode()),
            new SimpleBooleanProperty(settings.d3().view3DEnabled()),
            new SimpleObjectProperty<>(settings.d3().mazeFloorColor()),
            new SimpleObjectProperty<>(settings.d3().mazeLightColor()),
            new SimpleDoubleProperty(settings.d3().mazeWallHeight()),
            new SimpleDoubleProperty(settings.d3().mazeWallOpacity())
        );
    }
}


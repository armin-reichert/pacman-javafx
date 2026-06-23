package de.amr.pacmanfx.ui.viewmodel;

import de.amr.pacmanfx.ui.config.ui.UISettings;
import javafx.beans.property.*;
import javafx.util.Duration;

/**
 * UI settings view model.
 */
public class UISettingsVM {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewVM miniView;
    public final UISettings2DVM d2;
    public final UISettings3DVM d3;
    public final Maze3DSettingsVM maze3D;

    public UISettingsVM() {
        flashMessageDurationProperty = new SimpleObjectProperty<>();
        debugModeOnProperty = new SimpleBooleanProperty();
        keyboardMonitorOnProperty = new SimpleBooleanProperty();
        mutedProperty = new SimpleBooleanProperty();
        numSimulationStepsProperty = new SimpleIntegerProperty();

        miniView = new MiniViewVM();
        d2 = new UISettings2DVM();
        d3 = new UISettings3DVM();
        maze3D = new Maze3DSettingsVM();
    }

    public void init(UISettings settings) {
        flashMessageDurationProperty.set(Duration.seconds(settings.flashMessageDuration()));
        debugModeOnProperty.set(settings.debugModeOn());
        keyboardMonitorOnProperty.set(settings.keyboardMonitorOn());
        mutedProperty.set(settings.muted());
        numSimulationStepsProperty.set(settings.numSimulationSteps());

        miniView.init(settings.miniView());
        d2.init(settings.d2());
        d3.init(settings.d3());
        // maze3D is initialized elsewhere because it can be game-variant specific!
    }
}

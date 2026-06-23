package de.amr.pacmanfx.ui.viewmodel;

import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import javafx.beans.property.*;
import javafx.util.Duration;

public class GameUIViewModel {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewViewModel miniView;
    public final Settings2DViewModel d2;
    public final Settings3DViewModel d3;
    public final Maze3DSettingsViewModel maze3D;

    public GameUIViewModel() {
        flashMessageDurationProperty = new SimpleObjectProperty<>();
        debugModeOnProperty = new SimpleBooleanProperty();
        keyboardMonitorOnProperty = new SimpleBooleanProperty();
        mutedProperty = new SimpleBooleanProperty();
        numSimulationStepsProperty = new SimpleIntegerProperty();

        miniView = new MiniViewViewModel();
        d2 = new Settings2DViewModel();
        d3 = new Settings3DViewModel();
        maze3D = new Maze3DSettingsViewModel();
    }

    public void init(GameUISettings settings) {
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

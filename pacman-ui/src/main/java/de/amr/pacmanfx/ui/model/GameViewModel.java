package de.amr.pacmanfx.ui.model;

import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import javafx.beans.property.*;
import javafx.util.Duration;

public class GameViewModel {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewViewModel miniView;
    public final CommonSettings2DModel common2D;
    public final CommonSettings3DModel common3D;
    public final Maze3DSettingsModel maze3D;

    public GameViewModel() {
        flashMessageDurationProperty = new SimpleObjectProperty<>();
        debugModeOnProperty = new SimpleBooleanProperty();
        keyboardMonitorOnProperty = new SimpleBooleanProperty();
        mutedProperty = new SimpleBooleanProperty();
        numSimulationStepsProperty = new SimpleIntegerProperty();

        miniView = new MiniViewViewModel();
        common2D = new CommonSettings2DModel();
        common3D = new CommonSettings3DModel();
        maze3D = new Maze3DSettingsModel();
    }

    public void init(GameUISettings settings) {
        flashMessageDurationProperty.set(Duration.seconds(settings.flashMessageDuration()));
        debugModeOnProperty.set(settings.debugModeOn());
        keyboardMonitorOnProperty.set(settings.keyboardMonitorOn());
        mutedProperty.set(settings.muted());
        numSimulationStepsProperty.set(settings.numSimulationSteps());

        miniView.init(settings.miniView());
        common2D.init(settings.common2D());
        common3D.init(settings.common3D());
        // maze3D is initialized elsewhere because it can be game-variant specific!
    }
}

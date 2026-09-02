/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import javafx.beans.property.*;
import javafx.util.Duration;

public class GameViewModel {

    private final ObjectProperty<Duration> flashMessageDuration;
    private final BooleanProperty testStatesIncluded;
    private final BooleanProperty debugModeOn;
    private final BooleanProperty keyboardMonitorOn;
    private final BooleanProperty mute;
    private final IntegerProperty numSimulationSteps;

    private final MiniViewSettingsVM miniViewSettings;
    private final Game2DSettingsVM common2DSettings;
    private final Game3DSettingsVM common3DSettings;
    private final Maze3DSettingsVM maze3DSettings;

    public GameViewModel() {
        flashMessageDuration = new SimpleObjectProperty<>();
        testStatesIncluded = new SimpleBooleanProperty();
        debugModeOn = new SimpleBooleanProperty();
        keyboardMonitorOn = new SimpleBooleanProperty();
        mute = new SimpleBooleanProperty();
        numSimulationSteps = new SimpleIntegerProperty(1);

        miniViewSettings = new MiniViewSettingsVM();
        common2DSettings = new Game2DSettingsVM();
        common3DSettings = new Game3DSettingsVM();
        maze3DSettings = new Maze3DSettingsVM();
    }

    public void init(GameUISettings settings) {
        flashMessageDuration.set(Duration.seconds(settings.flashMessageDuration()));
        testStatesIncluded.set(settings.testStatesIncluded());
        debugModeOn.set(settings.debugModeOn());
        keyboardMonitorOn.set(settings.keyboardMonitorOn());
        mute.set(settings.muted());

        miniViewSettings.init(settings.miniView());
        common2DSettings.init(settings.common2D());
        common3DSettings.init(settings.common3D());

        // maze3D is initialized elsewhere because it can be game-variant specific!
    }

    public ObjectProperty<Duration> flashMessageDurationProperty() {
        return flashMessageDuration;
    }

    public BooleanProperty testStatesIncludedProperty() {
        return testStatesIncluded;
    }

    public BooleanProperty debugModeOnProperty() {
        return debugModeOn;
    }

    public BooleanProperty keyboardMonitorOnProperty() {
        return keyboardMonitorOn;
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public IntegerProperty numSimulationStepsProperty() {
        return numSimulationSteps;
    }

    public MiniViewSettingsVM miniViewSettings() {
        return miniViewSettings;
    }

    public Game2DSettingsVM common2DSettings() {
        return common2DSettings;
    }

    public Game3DSettingsVM common3DSettings() {
        return common3DSettings;
    }

    public Maze3DSettingsVM maze3DSettings() {
        return maze3DSettings;
    }
}

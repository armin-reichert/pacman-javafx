/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;

import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import javafx.beans.property.*;
import javafx.util.Duration;

public class GameUISettingsVM {

    public final ObjectProperty<Duration> flashMessageDurationProperty;
    public final BooleanProperty testStatesIncludedProperty;
    public final BooleanProperty debugModeOnProperty;
    public final BooleanProperty keyboardMonitorOnProperty;
    public final BooleanProperty mutedProperty;
    public final IntegerProperty numSimulationStepsProperty;

    public final MiniViewSettingsVM miniView;
    public final Common2DSettingsVM common2D;
    public final Common3DSettingsVM common3D;
    public final Maze3DSettingsVM maze3D;

    public GameUISettingsVM() {
        flashMessageDurationProperty = new SimpleObjectProperty<>();
        testStatesIncludedProperty = new SimpleBooleanProperty();
        debugModeOnProperty = new SimpleBooleanProperty();
        keyboardMonitorOnProperty = new SimpleBooleanProperty();
        mutedProperty = new SimpleBooleanProperty();
        numSimulationStepsProperty = new SimpleIntegerProperty(1);

        miniView = new MiniViewSettingsVM();
        common2D = new Common2DSettingsVM();
        common3D = new Common3DSettingsVM();
        maze3D = new Maze3DSettingsVM();
    }

    public void init(GameUISettings settings) {
        flashMessageDurationProperty.set(Duration.seconds(settings.flashMessageDuration()));
        testStatesIncludedProperty.set(settings.testStatesIncluded());
        debugModeOnProperty.set(settings.debugModeOn());
        keyboardMonitorOnProperty.set(settings.keyboardMonitorOn());
        mutedProperty.set(settings.muted());

        miniView.init(settings.miniView());
        common2D.init(settings.common2D());
        common3D.init(settings.common3D());

        // maze3D is initialized elsewhere because it can be game-variant specific!
    }
}

/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui2d.lib.NightMode;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public interface GlobalProperties2d {
    BooleanProperty PY_AUTOPILOT               = new SimpleBooleanProperty(false);
    ObjectProperty<Color> PY_CANVAS_BG_COLOR   = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty PY_CANVAS_FONT_SMOOTHING   = new SimpleBooleanProperty(false);
    BooleanProperty PY_CANVAS_IMAGE_SMOOTHING  = new SimpleBooleanProperty(false);
    BooleanProperty PY_DEBUG_INFO_VISIBLE      = new SimpleBooleanProperty(false);
    BooleanProperty PY_IMMUNITY                = new SimpleBooleanProperty(false);
    ObjectProperty<MapSelectionMode> PY_MAP_SELECTION_MODE = new SimpleObjectProperty<>(MapSelectionMode.CUSTOM_MAPS_FIRST);
    ObjectProperty<NightMode> PY_NIGHT_MODE    = new SimpleObjectProperty<>(NightMode.AUTO);
    IntegerProperty PY_PIP_HEIGHT              = new SimpleIntegerProperty(8*36);
    BooleanProperty PY_PIP_ON                  = new SimpleBooleanProperty(false);
    IntegerProperty PY_PIP_OPACITY_PERCENT     = new SimpleIntegerProperty(100);
    IntegerProperty PY_SIMULATION_STEPS        = new SimpleIntegerProperty(1);
}

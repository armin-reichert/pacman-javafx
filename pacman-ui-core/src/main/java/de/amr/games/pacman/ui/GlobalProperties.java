package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui.lib.NightMode;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public interface GlobalProperties {
    BooleanProperty PY_AUTOPILOT = new SimpleBooleanProperty(false);
    BooleanProperty PY_IMMUNITY  = new SimpleBooleanProperty(false);
    ObjectProperty<Color> PY_CANVAS_BG_COLOR      = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty PY_CANVAS_IMAGE_SMOOTHING     = new SimpleBooleanProperty(false);
    BooleanProperty PY_CANVAS_FONT_SMOOTHING      = new SimpleBooleanProperty(false);
    BooleanProperty PY_DEBUG_INFO_VISIBLE         = new SimpleBooleanProperty(false);
    ObjectProperty<NightMode> PY_NIGHT_MODE       = new SimpleObjectProperty<>(NightMode.AUTO);
    IntegerProperty PY_PIP_HEIGHT                 = new SimpleIntegerProperty(8*36);
    BooleanProperty PY_PIP_ON                     = new SimpleBooleanProperty(false);
    IntegerProperty PY_PIP_OPACITY_PERCENT        = new SimpleIntegerProperty(100);
    IntegerProperty PY_SIMULATION_STEPS           = new SimpleIntegerProperty(1);
}
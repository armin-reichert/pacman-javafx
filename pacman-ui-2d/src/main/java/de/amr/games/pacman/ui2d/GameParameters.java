/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameModel;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public interface GameParameters {

    BooleanProperty PY_AUTOPILOT           = new SimpleBooleanProperty(false);
    ObjectProperty<Color> PY_CANVAS_COLOR  = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty PY_CANVAS_DECORATED    = new SimpleBooleanProperty(true);
    BooleanProperty PY_DEBUG_INFO          = new SimpleBooleanProperty(false);
    BooleanProperty PY_IMMUNITY            = new SimpleBooleanProperty(false);
    BooleanProperty PY_NIGHT_MODE          = new SimpleBooleanProperty(false);
    IntegerProperty PY_PIP_HEIGHT          = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_Y);
    BooleanProperty PY_PIP_ON              = new SimpleBooleanProperty(false);
    IntegerProperty PY_PIP_OPACITY_PERCENT = new SimpleIntegerProperty(100);
    IntegerProperty PY_SIMULATION_STEPS    = new SimpleIntegerProperty(1);
}

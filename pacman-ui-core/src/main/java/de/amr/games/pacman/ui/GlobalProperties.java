package de.amr.games.pacman.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public interface GlobalProperties {
    BooleanProperty PY_AUTOPILOT = new SimpleBooleanProperty(false);
    BooleanProperty PY_IMMUNITY  = new SimpleBooleanProperty(false);
}

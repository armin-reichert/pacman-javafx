/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.MapSelectionMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public interface GlobalProperties2d extends de.amr.games.pacman.ui.GlobalProperties {
    ObjectProperty<MapSelectionMode> PY_MAP_SELECTION_MODE = new SimpleObjectProperty<>(MapSelectionMode.CUSTOM_MAPS_FIRST);
}

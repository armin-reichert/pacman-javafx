/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public interface TengenMsPacMan_Properties {
    BooleanProperty                  PROPERTY_JOYPAD_BINDINGS_DISPLAYED = new SimpleBooleanProperty(false);
    ObjectProperty<SceneDisplayMode> PROPERTY_PLAY_SCENE_DISPLAY_MODE = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);
}

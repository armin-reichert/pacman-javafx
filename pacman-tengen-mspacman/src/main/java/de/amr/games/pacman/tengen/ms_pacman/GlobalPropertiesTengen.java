package de.amr.games.pacman.tengen.ms_pacman;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public interface GlobalPropertiesTengen {
    BooleanProperty PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED = new SimpleBooleanProperty(false);
    ObjectProperty<SceneDisplayMode> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);
}

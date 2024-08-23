/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameParameters;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public interface GameParameters3D extends GameParameters {
    String NO_TEXTURE = "No Texture";

    BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(true);
    BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.web("#202020"));
    StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty(NO_TEXTURE);
    ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    ObjectProperty<Perspective> PY_3D_PERSPECTIVE        = new SimpleObjectProperty<>(Perspective.FOLLOWING_PLAYER);
    DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(0.9);
}

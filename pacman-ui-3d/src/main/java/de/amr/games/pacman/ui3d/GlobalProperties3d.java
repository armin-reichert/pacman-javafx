package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui3d.scene3d.Perspective;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public interface GlobalProperties3d {

    String NO_TEXTURE = "No Texture";

    BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(false);
    BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty(NO_TEXTURE);
    ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    ObjectProperty<Perspective.Name> PY_3D_PERSPECTIVE   = new SimpleObjectProperty<>(Perspective.Name.TRACK_PLAYER);
    DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(1.0);
}
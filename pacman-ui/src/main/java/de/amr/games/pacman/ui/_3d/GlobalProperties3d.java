package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui._3d.scene3d.Perspective;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.Globals.TS;

public interface GlobalProperties3d {

    int   LIVES_COUNTER_MAX     = 5;
    float LIVES_COUNTER_SIZE    = 12f;
    float FLOOR_THICKNESS       = 0.5f;
    float OBSTACLE_BASE_HEIGHT  = 7.0f;
    float OBSTACLE_TOP_HEIGHT   = 0.1f;
    float OBSTACLE_THICKNESS    = 1.25f;
    float HOUSE_BASE_HEIGHT     = 12.0f;
    float HOUSE_WALL_TOP_HEIGHT = 0.1f;
    float HOUSE_WALL_THICKNESS  = 1.5f;
    float HOUSE_OPACITY         = 0.4f;
    float HOUSE_SENSITIVITY     = 1.5f * TS;
    float PAC_SIZE              = 15.0f;
    float GHOST_SIZE            = 13.0f;
    float ENERGIZER_RADIUS      = 3.5f;
    float PELLET_RADIUS         = 1.0f;

    BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(false);
    BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    ObjectProperty<Perspective.Name> PY_3D_PERSPECTIVE   = new SimpleObjectProperty<>(Perspective.Name.TRACK_PLAYER);
    DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(1.0);
}
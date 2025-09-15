/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.layout.GameUI_View;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public class GameUI_Properties {
    public static final ObjectProperty<Color>            PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty                  PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);
    public static final ObjectProperty<GameScene>        PROPERTY_CURRENT_GAME_SCENE = new SimpleObjectProperty<>();
    public static final ObjectProperty<GameUI_View>      PROPERTY_CURRENT_VIEW = new SimpleObjectProperty<>();
    public static final BooleanProperty                  PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);
    public static final IntegerProperty                  PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);
    public static final BooleanProperty                  PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    public static final IntegerProperty                  PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);
    public static final BooleanProperty                  PROPERTY_MUTED = new SimpleBooleanProperty(false);
    public static final IntegerProperty                  PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);
    public static final BooleanProperty                  PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode>         PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty                  PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color>            PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    public static final ObjectProperty<Color>            PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);
    public static final ObjectProperty<PerspectiveID>    PROPERTY_3D_PERSPECTIVE = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    public static final DoubleProperty                   PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();
    public static final DoubleProperty                   PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);
}

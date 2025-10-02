/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui._3d.PerspectiveID;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public interface GameUI_Properties {
    ObjectProperty<Color>         PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty               PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty               PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);
    BooleanProperty               PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);
    BooleanProperty               PROPERTY_MUTED = new SimpleBooleanProperty(false);
    IntegerProperty               PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);
    BooleanProperty               PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>      PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty               PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);
    ObjectProperty<Color>         PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>         PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);
    ObjectProperty<PerspectiveID> PROPERTY_3D_PERSPECTIVE_ID = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    DoubleProperty                PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();
    DoubleProperty                PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);
}
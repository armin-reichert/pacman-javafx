/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;

public interface Settings3D {
    float BONUS_3D_SYMBOL_WIDTH       = TS;
    float BONUS_3D_POINTS_WIDTH       = 1.8f * TS;
    float ENERGIZER_3D_MIN_SCALING    = 0.2f;
    float ENERGIZER_3D_MAX_SCALING    = 1.0f;
    float ENERGIZER_3D_RADIUS         = 3.5f;
    float FLOOR_3D_PADDING            = 5.0f;
    float FLOOR_3D_THICKNESS          = 0.5f;
    float GHOST_3D_SIZE               = 16.0f;
    float HOUSE_3D_BASE_HEIGHT        = 12.0f;
    float HOUSE_3D_OPACITY            = 0.4f;
    float HOUSE_3D_SENSITIVITY        = 1.5f * TS;
    float HOUSE_3D_WALL_THICKNESS     = 2.5f;
    byte  LIVES_COUNTER_3D_CAPACITY   = 5;
    float LIVES_COUNTER_3D_SHAPE_SIZE = 12f;
    Color LIVES_COUNTER_PILLAR_COLOR  = Color.grayRgb(120);
    Color LIVES_COUNTER_PLATE_COLOR   = Color.grayRgb(180);
    float OBSTACLE_3D_BASE_HEIGHT     = 7.0f;
    float OBSTACLE_3D_WALL_THICKNESS  = 2.25f;
    float PAC_3D_SIZE                 = 17.0f;
    float PELLET_3D_RADIUS            = 1.0f;
}

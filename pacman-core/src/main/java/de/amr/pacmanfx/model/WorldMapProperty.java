/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public interface WorldMapProperty {
    String POS_BONUS = "pos_bonus";
    String POS_PAC = "pos_pac";
    String POS_RED_GHOST = "pos_ghost_1_red";
    String POS_PINK_GHOST = "pos_ghost_2_pink";
    String POS_CYAN_GHOST = "pos_ghost_3_cyan";
    String POS_ORANGE_GHOST = "pos_ghost_4_orange";
    String POS_SCATTER_RED_GHOST = "pos_scatter_ghost_1_red";
    String POS_SCATTER_PINK_GHOST = "pos_scatter_ghost_2_pink";
    String POS_SCATTER_CYAN_GHOST = "pos_scatter_ghost_3_cyan";
    String POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";
    String POS_HOUSE_MIN_TILE = "pos_house_min";
    String POS_HOUSE_MAX_TILE = "pos_house_max";

    String COLOR_FOOD = "color_food";
    String COLOR_WALL_STROKE = "color_wall_stroke";
    String COLOR_WALL_FILL = "color_wall_fill";
    String COLOR_DOOR = "color_door";
}

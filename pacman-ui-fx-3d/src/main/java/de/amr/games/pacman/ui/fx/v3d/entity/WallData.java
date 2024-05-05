/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

/**
 * @author Armin Reichert
 */
public class WallData {
    byte type; // see FloorPlan
    short x;
    short y;
    short numBricksX;
    short numBricksY;
}

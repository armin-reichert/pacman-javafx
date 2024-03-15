/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

/**
 * @author Armin Reichert
 */
public class WallData {
    byte type; // see FloorPlan
    int x;
    int y;
    int numBricksX;
    int numBricksY;
}

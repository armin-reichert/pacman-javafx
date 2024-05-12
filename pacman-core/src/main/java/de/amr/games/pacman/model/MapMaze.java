/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

/**
 * Map-maze combination.
 *
 * @param mapNumber map number (starting with 1)
 * @param mazeNumber maze number (starting with 1)
 */
public record MapMaze(int mapNumber, int mazeNumber) {
}

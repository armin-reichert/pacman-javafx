/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import java.util.List;

public record MazeSet(ColoredMaze normalMaze, List<ColoredMaze> flashingMazes) {
}

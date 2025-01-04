/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.tengen.ms_pacman.maps;

import java.util.List;

public record ColoredMapSet(ColoredMapImage normalMaze, List<ColoredMapImage> flashingMazes) {
}

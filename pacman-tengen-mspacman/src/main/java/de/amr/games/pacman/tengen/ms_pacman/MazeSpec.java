/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.nes.NES_ColorScheme;

public record MazeSpec(MapCategory mapCategory, int spriteNumber, NES_ColorScheme colorScheme) {}

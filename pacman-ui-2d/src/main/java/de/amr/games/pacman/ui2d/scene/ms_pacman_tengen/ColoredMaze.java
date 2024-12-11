/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColoredMaze(Image source, RectArea area, NES_ColorScheme colorScheme) {}

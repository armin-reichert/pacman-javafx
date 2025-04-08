/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColoredMapImage(Image source, RectArea region, NES_ColorScheme colorScheme) {}

/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.maps;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColoredMapImage(Image source, RectArea area, NES_ColorScheme colorScheme) {}

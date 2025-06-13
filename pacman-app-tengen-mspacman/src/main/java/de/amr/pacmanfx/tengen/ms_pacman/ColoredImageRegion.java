/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColoredImageRegion(Image image, Sprite region, NES_ColorScheme colorScheme) {}

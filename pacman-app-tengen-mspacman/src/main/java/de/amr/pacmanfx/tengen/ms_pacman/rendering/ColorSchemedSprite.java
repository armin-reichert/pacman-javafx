/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColorSchemedSprite(Image image, Sprite sprite, NES_ColorScheme colorScheme) {}

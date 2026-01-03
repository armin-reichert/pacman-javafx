/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColorSchemedImage(Image spriteSheetImage, RectShort sprite, NES_ColorScheme colorScheme) {}

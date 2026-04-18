/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.nes.NES_ColorScheme;
import javafx.scene.image.Image;

public record ColorSchemedImage(Image spriteSheetImage, RectShort sprite, NES_ColorScheme colorScheme) {}

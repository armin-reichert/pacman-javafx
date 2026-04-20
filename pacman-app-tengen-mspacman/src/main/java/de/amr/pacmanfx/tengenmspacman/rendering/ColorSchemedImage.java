/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import javafx.scene.image.Image;

public record ColorSchemedImage(Image spriteSheetImage, RectShort sprite, NES_MapColorScheme colorScheme) {}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.math.RectShort;
import javafx.scene.image.Image;

public record ColorSchemedMapSprite(Image spriteSheetImage, RectShort sprite, NES_MapColorScheme colorScheme) {}

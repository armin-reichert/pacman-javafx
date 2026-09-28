/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.assets;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;

/**
 * Sprite with a facing direction.
 *
 * @param sprite a sprite
 * @param facing the current facing direction
 */
public record FacingSprite(RectShort sprite, Direction facing) {}

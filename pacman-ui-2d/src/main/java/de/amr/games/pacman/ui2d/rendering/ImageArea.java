/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.RectArea;
import javafx.scene.image.Image;

/**
 * A rectangular area in an image e.g. a sprite sheet source image.
 *
 * @author Armin Reichert
 *
 * @param source source image
 * @param area rectangular area
 */
public record ImageArea(Image source, RectArea area) {
}

/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Disposable;

import java.util.List;

public record ColoredMazeSpriteSet(
    ColoredSpriteImage mazeImage,
    List<ColoredSpriteImage> flashingMazeImages) implements Disposable {

    @Override
    public void dispose() {
        if (flashingMazeImages != null) {
            flashingMazeImages.clear();
        }
    }

    @Override
    public String toString() {
        return "ColoredMazeSpriteSet{"
            + "mazeImage=" + mazeImage
            + ", flashingMazeImages=" + flashingMazeImages
            + "}";
    }
}

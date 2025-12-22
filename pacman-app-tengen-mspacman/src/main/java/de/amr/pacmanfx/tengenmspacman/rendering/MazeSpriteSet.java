/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.Disposable;

import java.util.List;

public record MazeSpriteSet(ColoredSpriteImage mazeImage, List<ColoredSpriteImage> flashingMazeImages) implements Disposable {

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

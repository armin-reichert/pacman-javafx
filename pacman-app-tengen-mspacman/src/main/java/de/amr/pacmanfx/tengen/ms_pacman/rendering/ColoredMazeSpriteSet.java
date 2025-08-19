/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Disposable;

import java.util.List;

public record ColoredMazeSpriteSet(
    ColoredSpriteImage mazeSprite,
    List<ColoredSpriteImage> flashingMazeSprites) implements Disposable {

    @Override
    public void dispose() {
        if (flashingMazeSprites != null) {
            flashingMazeSprites.clear();
        }
    }
}

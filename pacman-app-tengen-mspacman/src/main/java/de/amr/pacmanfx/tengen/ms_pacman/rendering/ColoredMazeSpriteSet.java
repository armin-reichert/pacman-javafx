/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import java.util.List;

public record ColoredMazeSpriteSet(RecoloredSpriteImage colorSchemedMazeSprite, List<RecoloredSpriteImage> flashingMazeSprites) {}

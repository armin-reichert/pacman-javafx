/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.playscene;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;

public record GameLevelRenderable(GameLevel level, InfoMap renderInfo) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}

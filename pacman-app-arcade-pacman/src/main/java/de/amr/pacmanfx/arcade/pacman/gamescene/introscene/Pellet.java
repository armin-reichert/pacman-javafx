/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

class Pellet extends GameEntity implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}

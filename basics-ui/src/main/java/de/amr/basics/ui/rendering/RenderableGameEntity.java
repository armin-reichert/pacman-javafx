/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.ui.ecs.GameEntity;

import static java.util.Objects.requireNonNull;

public record RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) implements Renderable {

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        this.gameEntity = requireNonNull(gameEntity);
        this.layer = requireNonNull(layer);
        this.z = z;
    }

    public RenderableGameEntity change(RenderingLayer newLayer) {
        return new RenderableGameEntity(gameEntity, newLayer, z);
    }

    public RenderableGameEntity change(RenderingLayer newLayer, int newZ) {
        return new RenderableGameEntity(gameEntity, newLayer, newZ);
    }
}

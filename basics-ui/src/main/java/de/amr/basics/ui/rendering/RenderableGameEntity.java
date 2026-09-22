/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.GameEntity;

import static java.util.Objects.requireNonNull;

public record RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z, Vector2f offset) implements Renderable {

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z, Vector2f offset) {
        this.gameEntity = requireNonNull(gameEntity);
        this.layer = requireNonNull(layer);
        this.z = z;
        this.offset = offset;
    }

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        this(gameEntity, layer, z, Vector2f.ZERO);
    }

    public RenderableGameEntity change(RenderingLayer newLayer) {
        return new RenderableGameEntity(gameEntity, newLayer, z);
    }

    public RenderableGameEntity change(RenderingLayer newLayer, int newZ) {
        return new RenderableGameEntity(gameEntity, newLayer, newZ);
    }

    public RenderableGameEntity offset(Vector2f offset) {
        return new RenderableGameEntity(gameEntity, layer, z, offset);
    }
}

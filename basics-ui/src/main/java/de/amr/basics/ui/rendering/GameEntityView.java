/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ecs.GameEntity;

import static java.util.Objects.requireNonNull;

public record GameEntityView(GameEntity entity, RenderingLayer layer, int z, Vector2f offset) implements Renderable {

    public GameEntityView(GameEntity entity, RenderingLayer layer, int z, Vector2f offset) {
        this.entity = requireNonNull(entity);
        this.layer = requireNonNull(layer);
        this.z = z;
        this.offset = offset;
    }

    public GameEntityView(GameEntity entity, RenderingLayer layer, int z) {
        this(entity, layer, z, Vector2f.ZERO);
    }

    public GameEntityView newLayer(RenderingLayer newLayer) {
        return new GameEntityView(entity, newLayer, z);
    }

    public GameEntityView newLayer(RenderingLayer newLayer, int newZ) {
        return new GameEntityView(entity, newLayer, newZ);
    }

    public GameEntityView newOffset(Vector2f offset) {
        return new GameEntityView(entity, layer, z, offset);
    }
}

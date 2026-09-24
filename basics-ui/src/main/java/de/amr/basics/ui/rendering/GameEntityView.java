/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;

import static java.util.Objects.requireNonNull;

public record GameEntityView(GameEntity entity, RenderingLayer layer, int z, Vector2f offset, InfoMap renderInfo) implements Renderable {

    public GameEntityView(GameEntity entity, RenderingLayer layer, int z, Vector2f offset, InfoMap renderInfo) {
        this.entity = requireNonNull(entity);
        this.layer = requireNonNull(layer);
        this.z = z;
        this.offset = requireNonNull(offset);
        this.renderInfo = renderInfo;
    }

    public GameEntityView(GameEntity entity, RenderingLayer layer, int z, Vector2f offset) {
        this(entity, layer, z, offset, null);
    }

    public GameEntityView newOffset(Vector2f offset) {
        return new GameEntityView(entity, layer, z, offset);
    }
}

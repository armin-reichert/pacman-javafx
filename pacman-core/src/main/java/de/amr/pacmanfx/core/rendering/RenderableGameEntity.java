/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rendering;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

import static java.util.Objects.requireNonNull;

public record RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) implements Renderable {

    public static RenderableGameEntity renderableActor(GameEntity gameEntity) {
        return new RenderableGameEntity(gameEntity, RenderingLayer.ACTORS, 0);
    }

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        this.gameEntity = requireNonNull(gameEntity);
        this.layer = requireNonNull(layer);
        this.z = z;
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rendering;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;

import static java.util.Objects.requireNonNull;

public record RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) implements Renderable {

    //TODO move elsewhere
    public static int PAC_Z = 0;

    public static int ghostZ(GhostPersonality p) {
        return switch (p) {
            case RED_GHOST_SHADOW   -> 13; // on top of all other ghosts
            case PINK_GHOST_SPEEDY  -> 12;
            case CYAN_GHOST_BASHFUL -> 11;
            case ORANGE_GHOST_POKEY -> 10; // behind all other ghosts
            default -> 10;
        };
    }

    public static RenderableGameEntity renderableActor(GameEntity gameEntity) {
        return new RenderableGameEntity(gameEntity, RenderingLayer.ACTORS, 0);
    }

    public static RenderableGameEntity renderableActor(GameEntity gameEntity, int z) {
        return new RenderableGameEntity(gameEntity, RenderingLayer.ACTORS, z);
    }

    public static RenderableGameEntity renderableGhost(Ghost ghost) {
        return new RenderableGameEntity(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()));
    }

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        this.gameEntity = requireNonNull(gameEntity);
        this.layer = requireNonNull(layer);
        this.z = z;
    }
}

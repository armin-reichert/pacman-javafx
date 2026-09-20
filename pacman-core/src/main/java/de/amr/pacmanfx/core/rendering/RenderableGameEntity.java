/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rendering;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;

import static java.util.Objects.requireNonNull;

public record RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) implements Renderable {

    //TODO move elsewhere

    private static final int PAC_Z = 0;
    private static final int BONUS_Z = -1;

    private static int ghostZ(GhostPersonality p) {
        return switch (p) {
            case RED_GHOST_SHADOW   -> 13; // on top of all other ghosts
            case PINK_GHOST_SPEEDY  -> 12;
            case CYAN_GHOST_BASHFUL -> 11;
            case ORANGE_GHOST_POKEY -> 10; // behind all other ghosts
        };
    }

    public static RenderableGameEntity renderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        return new RenderableGameEntity(gameEntity, layer, z);
    }

    public static RenderableGameEntity renderablePac(Pac pac) {
        return renderableGameEntity(pac, RenderingLayer.ACTORS, PAC_Z);
    }

    public static RenderableGameEntity renderableGhost(Ghost ghost) {
        return renderableGameEntity(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()));
    }

    public static RenderableGameEntity renderableBonus(Bonus bonus) {
        return renderableGameEntity(bonus, RenderingLayer.ACTORS, BONUS_Z);
    }

    public RenderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        this.gameEntity = requireNonNull(gameEntity);
        this.layer = requireNonNull(layer);
        this.z = z;
    }
}

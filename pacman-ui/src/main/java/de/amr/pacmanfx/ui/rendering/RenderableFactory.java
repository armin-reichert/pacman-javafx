/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;

public final class RenderableFactory {

    private RenderableFactory() {}

    public static final int PAC_Z = 0;
    public static final int BONUS_Z = -1;

    public static int ghostZ(GhostPersonality p) {
        return switch (p) {
            case RED_GHOST_SHADOW   -> 13; // on top of all other ghosts
            case PINK_GHOST_SPEEDY  -> 12;
            case CYAN_GHOST_BASHFUL -> 11;
            case ORANGE_GHOST_POKEY -> 10; // behind all other ghosts
        };
    }

    public static GameEntityView createGameEntityView(GameEntity gameEntity, RenderingLayer layer, int z, Vector2f offset, InfoMap renderInfo) {
        return new GameEntityView(gameEntity, layer, z, offset, renderInfo);
    }

    public static GameEntityView createPropView(GameEntity gameEntity, int z) {
        return createGameEntityView(gameEntity, RenderingLayer.PROPS, z, Vector2f.ZERO, null);
    }

    public static GameEntityView createPropView(GameEntity gameEntity) {
        return createPropView(gameEntity, 0);
    }

    public static GameEntityView createPacView(Pac pac) {
        return createGameEntityView(pac, RenderingLayer.ACTORS, PAC_Z, Vector2f.ZERO, null);
    }

    public static GameEntityView createGhostView(Ghost ghost) {
        return createGameEntityView(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()), Vector2f.ZERO, null);
    }

    public static GameEntityView createBonusView(Bonus bonus) {
        return createGameEntityView(bonus, RenderingLayer.ACTORS, BONUS_Z, Vector2f.ZERO, null);
    }
}

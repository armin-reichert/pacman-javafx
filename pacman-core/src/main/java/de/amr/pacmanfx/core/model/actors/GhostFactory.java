/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public class GhostFactory {

    public static Ghost createRedGhostShadow(String name) {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, name);
        registerCommonComponents(ghost);
        ghost.registerComponent(Elroy.class, new Elroy());
        ghost.reset();
        return ghost;
    }

    public static Ghost createPinkGhostAmbusher(String name) {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, name);
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public static Ghost createCyanGhostBashful(String name) {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, name);
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public static Ghost createOrangeGhostPokey(String name) {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, name);
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    private static void registerCommonComponents(Ghost ghost) {
        ghost.registerComponent(Movement.class, new Movement());
        ghost.registerComponent(WorldNavigation.class, new WorldNavigation());
        ghost.registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        ghost.registerComponent(GhostStateComponent.class, new GhostStateComponent());
        ghost.registerComponent(SpriteAnim.class, new SpriteAnim());
        //TODO where does this belong?
        ghost.worldNavigation().corneringSpeedDelta = -1.25f;
    }
}

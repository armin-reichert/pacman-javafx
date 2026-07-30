/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;

public final class TengenMsPacMan_ActorFactory {

    private static class SingletonHolder {
        static final TengenMsPacMan_ActorFactory SINGLETON = new TengenMsPacMan_ActorFactory();
    }

    public static TengenMsPacMan_ActorFactory instance() {
        return SingletonHolder.SINGLETON;
    }

    private TengenMsPacMan_ActorFactory() {}

    public Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public Pac createMsPacMan() {
        final var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public Ghost createRedGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.RED_GHOST_SHADOW, "Blinky");
        registerCommonGhostComponents(ghost);
        ghost.setComponent(Elroy.class, new Elroy());
        ghost.reset();
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.PINK_GHOST_SPEEDY, "Pinky");
        registerCommonGhostComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createCyanGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.CYAN_GHOST_BASHFUL, "Inky");
        registerCommonGhostComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.ORANGE_GHOST_POKEY, "Sue");
        registerCommonGhostComponents(ghost);
        ghost.reset();
        return ghost;
    }

    private void registerCommonGhostComponents(Ghost ghost) {
        ghost.setComponent(Movement.class, new Movement());
        ghost.setComponent(WorldNavigation.class, new WorldNavigation());
        ghost.setComponent(GhostStateComponent.class, new GhostStateComponent());
        ghost.setComponent(GhostWorldPlacement.class, new  GhostWorldPlacement());
        ghost.setComponent(SpriteAnim.class, new SpriteAnim());

        //TODO where does this belong?
        ghost.worldNavigation().corneringSpeedDelta = -1.25f;
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;

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
        ghost.reset();
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.PINK_GHOST_SPEEDY, "Pinky");
        ghost.reset();
        return ghost;
    }

    public Ghost createCyanGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.CYAN_GHOST_BASHFUL, "Inky");
        ghost.reset();
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.ORANGE_GHOST_POKEY, "Sue");
        ghost.reset();
        return ghost;
    }
}

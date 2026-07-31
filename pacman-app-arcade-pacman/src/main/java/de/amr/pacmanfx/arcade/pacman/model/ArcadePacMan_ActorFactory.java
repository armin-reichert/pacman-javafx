/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.comp.ghost.ElroyComp;

public class ArcadePacMan_ActorFactory {

    private static class SingletonHolder {
        static final ArcadePacMan_ActorFactory SINGLETON = new ArcadePacMan_ActorFactory();
    }

    public static ArcadePacMan_ActorFactory instance() {
        return SingletonHolder.SINGLETON;
    }

    protected ArcadePacMan_ActorFactory() {}

    public Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public Ghost createRedGhost() {
        final Ghost ghost = new Ghost(GhostPersonality.RED_GHOST_SHADOW, "Blinky");
        ghost.setComponent(ElroyComp.class, new ElroyComp());
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
        final Ghost ghost = new Ghost(GhostPersonality.ORANGE_GHOST_POKEY, "Clyde");
        ghost.reset();
        return ghost;
    }
}

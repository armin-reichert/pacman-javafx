package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;

public class ArcadeMsPacMan_ActorFactory extends ArcadePacMan_ActorFactory {

    public Pac createMsPacMan() {
        return new Pac("Ms. Pac-Man");
    }

    public Ghost createRedGhost() {
        final Ghost ghost = super.createRedGhost();
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = super.createPinkGhost();
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = super.createOrangeGhost();
        ghost.setName("Sue");
        return ghost;
    }
}

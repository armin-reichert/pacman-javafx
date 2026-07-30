package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;

public class ArcadeMsPacMan_ActorFactory extends ArcadePacMan_ActorFactory {

    private static class SingletonHolder {
        static final ArcadeMsPacMan_ActorFactory SINGLETON = new ArcadeMsPacMan_ActorFactory();
    }

    public static ArcadeMsPacMan_ActorFactory instance() {
        return SingletonHolder.SINGLETON;
    }

    public Pac createMsPacMan() {
        return new Pac("Ms. Pac-Man");
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = super.createOrangeGhost();
        ghost.setName("Sue"); // The first LGBTQ+ ghost in history!
        return ghost;
    }
}

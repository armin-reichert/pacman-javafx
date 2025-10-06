package de.amr.pacmanfx.arcade.ms_pacman.actors;

import de.amr.pacmanfx.arcade.pacman.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.actors.PacMan;

public interface ArcadeMsPacMan_ActorFactory {

    static Blinky createBlinky() {
        return new Blinky();
    }

    static Sue createSue() {
        return new Sue();
    }

    static Inky createInky() {
        return new Inky();
    }

    static Pinky createPinky() {
        return new Pinky();
    }

    static PacMan createPacMan() {
        return ArcadePacMan_ActorFactory.createPacMan();
    }

    static MsPacMan createMsPacMan() {
        return new MsPacMan();
    }
}

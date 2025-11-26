package de.amr.pacmanfx.arcade.pacman.model.actors;

public interface ArcadePacMan_ActorFactory {

    static Blinky createBlinky() {
        return new Blinky();
    }

    static Clyde createClyde() {
        return new Clyde();
    }

    static Inky createInky() {
        return new Inky();
    }

    static Pinky createPinky() {
        return new Pinky();
    }

    static PacMan createPacMan() {
        return new PacMan();
    }
}

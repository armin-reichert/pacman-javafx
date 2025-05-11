package de.amr.pacmanfx.model.actors;

public interface Animations {
    // Common animation IDs for Pac-Man and Ms. Pac-Man
    String ANY_PAC_MUNCHING = "munching";
    String ANY_PAC_DYING = "dying";

    String currentID();
    void select(String id, int frameIndex);
    default void select(String id) { select(id, 0); }
    void start();
    void stop();
    void reset();
}

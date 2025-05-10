package de.amr.games.pacman.model.actors;

public interface Animations {
    String currentID();
    void select(String id, int frameIndex);
    default void select(String id) { select(id, 0); }
    void start();
    void stop();
    void reset();
}

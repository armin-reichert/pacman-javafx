/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

public interface ActorAnimations {
    String currentID();
    void selectAtFrame(String id, int frameIndex);
    default void select(String id) { selectAtFrame(id, 0); }
    void start();
    void stop();
    void reset();
}

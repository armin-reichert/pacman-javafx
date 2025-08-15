/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

public interface AnimationManager {
    Object animation(String id);
    String selectedID();
    void selectFrame(String id, int frameIndex);
    default void select(String id) { selectFrame(id, 0); }
    default void play(String id) { select(id); play(); }
    void play();
    void stop();
    void reset();
}

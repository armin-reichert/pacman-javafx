/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

public interface Animations {
    String currentID();
    void select(String id, int frameIndex);
    default void select(String id) { select(id, 0); }
    void start();
    void stop();
    void reset();
}

/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

public interface ActorAnimationSet {
    String selectedAnimationID();
    void selectAnimationAtFrame(String id, int frameIndex);
    default void selectAnimation(String id) { selectAnimationAtFrame(id, 0); }
    void play();
    void stop();
    void reset();
}

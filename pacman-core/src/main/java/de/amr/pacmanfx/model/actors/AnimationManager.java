/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.RectShort;

public interface AnimationManager {

    AnimationManager EMPTY = new AnimationManager() {
        @Override
        public Object animation(String id) {
            return null;
        }

        @Override
        public String selectedID() {
            return "";
        }

        @Override
        public void selectFrame(String id, int frameIndex) {
        }

        @Override
        public void play() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void reset() {
        }

        @Override
        public RectShort currentSprite(Actor actor) {
            return null;
        }

        @Override
        public int frameIndex() {
            return -1;
        }
    };

    Object animation(String id);
    String selectedID();
    void selectFrame(String id, int frameIndex);
    default void select(String id) { selectFrame(id, 0); }
    default void play(String id) { select(id); play(); }
    void play();
    void stop();
    void reset();
    RectShort currentSprite(Actor actor);
    int frameIndex();
}

/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.PerspectiveCamera;

/**
 * Play scene camera perspectives.
 */
public interface Perspective {

    enum ID {
        DRONE, TOTAL, TRACK_PLAYER, NEAR_PLAYER;

        public ID prev() {
            return values()[ordinal() == 0 ? values().length - 1 : ordinal() - 1];
        }
        public ID next() {
            return values()[ordinal() < values().length - 1 ? ordinal() + 1 : 0];
        }
    }

    void init(PerspectiveCamera camera);
 
    void update(PerspectiveCamera camera, GameLevel level, Actor spottedActor);

}
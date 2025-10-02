/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.GameContext;
import javafx.scene.PerspectiveCamera;

/**
 * Play scene camera perspectives.
 */
public interface Perspective {

    void attach(PerspectiveCamera camera);

    default void detach(PerspectiveCamera camera) {}

    void update(PerspectiveCamera camera, GameContext gameContext);
}
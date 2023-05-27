/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.scene.Camera;

/**
 * Cameras for the 3D play scene.
 * 
 * @author Armin Reichert
 */
public interface CameraController {

	default void reset(Camera cam) {
	}

	default void update(Camera cam, Pac3D pac3D) {
	}
}
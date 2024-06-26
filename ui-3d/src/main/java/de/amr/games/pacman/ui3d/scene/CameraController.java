/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene;

import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.world.World;
import javafx.scene.Camera;

/**
 * Camera controller interface for the 3D play scene.
 *
 * @author Armin Reichert
 */
public interface CameraController {

    default void init(Camera cam, World world) {
    }

    default void update(Camera cam, World world, Entity spottedEntity) {
    }
}
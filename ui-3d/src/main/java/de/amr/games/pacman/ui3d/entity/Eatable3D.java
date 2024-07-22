/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import javafx.animation.Animation;
import javafx.geometry.Point3D;
import javafx.scene.Node;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface Eatable3D {

    Node root();

    void onEaten();

    Optional<Animation> getEatenAnimation();

    Vector2i tile();

    void placeAtTile(Vector2i tile, double overGround);

    Point3D position();
}

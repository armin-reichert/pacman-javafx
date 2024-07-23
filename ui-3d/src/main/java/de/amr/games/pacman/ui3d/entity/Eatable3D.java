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

    void setTile(Vector2i tile);

    Vector2i tile();

    void setPosition(Point3D position);

    Point3D position();
}